package com.mihneapopescu.cookingrecipes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mihneapopescu.cookingrecipes.adapters.RecipeItemAdapter;
import com.mihneapopescu.cookingrecipes.auth.LoginActivity;
import com.mihneapopescu.cookingrecipes.items.RecipeItem;
import com.mihneapopescu.cookingrecipes.models.Ingredient;
import com.mihneapopescu.cookingrecipes.models.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecipeItemAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Realm
        realm = Realm.getDefaultInstance();

        if(currentUser == null) {
            // No user is signed in, redirect to LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();  // This prevents the user from being able to hit the back button to return to MainActivity
            return;
        }

        // Initialize recyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);

        // Set divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Fetch data from the server
        fetchData();
    }

    public void fetchData() {
        db.collection("recipes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalDocuments = task.getResult().size();
                        AtomicInteger processedDocuments = new AtomicInteger(0);

                        realm.executeTransaction(realm -> {
                            Recipe newAddedRecipe = null;
                            Boolean firstTime = (realm.where(Recipe.class).count() == 0);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Recipe recipe;

                                if(realm.where(Recipe.class).equalTo("id", document.getId()).count() == 0) {
                                    recipe = realm.createObject(Recipe.class, document.getId());
                                    recipe.setName(document.getString("name"));
                                    recipe.setDescription(document.getString("description"));
                                    recipe.setPhotoUrl(document.getString("photoUrl"));
                                    recipe.setYoutubeUrl(document.getString("youtubeUrl"));
                                    newAddedRecipe = recipe;
                                }
                                else {
                                    recipe = realm.where(Recipe.class).equalTo("id", document.getId()).findFirst();
                                }


                                // Get the ingredients subcollection
                                Recipe finalNewAddedRecipe = newAddedRecipe;
                                document.getReference().collection("ingredients")
                                        .get()
                                        .addOnCompleteListener(subTask -> {
                                            if (subTask.isSuccessful()) {
                                                for (QueryDocumentSnapshot ingredientDocument : subTask.getResult()) {
                                                    String ingredientId = ingredientDocument.getId();

                                                    if(realm.where(Ingredient.class).equalTo("id", ingredientId).count() == 0) {
                                                        realm.executeTransaction(realm1 -> {
                                                            Ingredient ingredient = ingredientDocument.toObject(Ingredient.class);
                                                            ingredient.setId(ingredientId);

                                                            // Save the ingredient to Realm
                                                            realm1.copyToRealmOrUpdate(ingredient);

                                                            recipe.getIngredients().add(ingredient);
                                                        });
                                                    }
                                                }
                                            } else {
                                                Log.d("INGREDIENTS", "Error getting ingredients: ", task.getException());
                                            }

                                            if(processedDocuments.incrementAndGet() == totalDocuments) {
                                                updateRecipeListUI();

                                                if(finalNewAddedRecipe != null && !firstTime) {
                                                    sendNewRecipeNotification(recipe);
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
    }

    private void updateRecipeListUI() {
        // Update Recipe List
        List<RecipeItem> recipeList = new ArrayList<>();

        RealmResults<Recipe> recipes = realm.where(Recipe.class).findAll();

        for(Recipe recipe : recipes) {
            recipeList.add(new RecipeItem(recipe.getId(), recipe.getName(), recipe.getDescription(), recipe.getPhotoUrl()));
        }

        adapter = new RecipeItemAdapter(recipeList, this);
        recyclerView.setAdapter(adapter);
    }

    private void sendNewRecipeNotification(Recipe recipe) {
        String channelId = "recipe_channel";
        String channelName = "Recipe Channel";

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // If the Android Version is greater than Oreo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher) // set icon here
                .setContentTitle("New Recipe Published: " + recipe.getName())
                .setContentText(recipe.getDescription())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }

    private boolean logOut() {
        FirebaseAuth.getInstance().signOut();

        // Delete shared preferences
        SharedPreferences sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("uid");
        editor.apply();

        // Delete realm data
        realm.executeTransaction(realm1 -> realm1.deleteAll());

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                return logOut();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}