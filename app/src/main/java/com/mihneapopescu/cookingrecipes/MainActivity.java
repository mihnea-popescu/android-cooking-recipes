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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.mihneapopescu.cookingrecipes.broadcast_receivers.NetworkChangeReceiver;
import com.mihneapopescu.cookingrecipes.items.RecipeItem;
import com.mihneapopescu.cookingrecipes.models.Ingredient;
import com.mihneapopescu.cookingrecipes.models.Recipe;
import com.mihneapopescu.cookingrecipes.models.Review;

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
    private NetworkChangeReceiver internetAvailabilityReceiver;

    // Broadcast receiver
    @Override
    protected void onStart() {
        super.onStart();
        // Create an instance of the broadcast receiver
        internetAvailabilityReceiver = new NetworkChangeReceiver();

        // Create an intent filter with the action you want to listen for
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");

        // Register the receiver with this activity and the intent filter
        registerReceiver(internetAvailabilityReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister the broadcast receiver
        if (internetAvailabilityReceiver != null) {
            unregisterReceiver(internetAvailabilityReceiver);
        }
    }


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

                                // Get the reviews subcollection
                                document.getReference().collection("reviews")
                                        .get()
                                        .addOnCompleteListener(subTask -> {
                                           if(subTask.isSuccessful()) {
                                                for(QueryDocumentSnapshot reviewDocument : subTask.getResult()) {
                                                    String reviewId = reviewDocument.getId();

                                                    if(realm.where(Review.class).equalTo("id", reviewId).count() > 0) {
                                                        continue;
                                                    }

                                                    realm.executeTransaction(realm1 -> {
                                                        Review review = reviewDocument.toObject(Review.class);
                                                        review.setId(reviewId);
                                                        review.setRecipe(recipe);

                                                        recipe.getReviews().add(review);
                                                    });
                                                }
                                           }
                                           else {
                                               Log.d("REVIEWS", "Error getting reviews: ", task.getException());
                                           }
                                        });

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
                                                    sendNewRecipeNotification(finalNewAddedRecipe);
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

        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("RECIPE_ID", recipe.getId());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher) // set icon here
                .setContentTitle("New Recipe Published: " + recipe.getName())
                .setContentText(recipe.getDescription())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

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
        // Use activity's root decor view to get window token
        View view = activity.getWindow().getDecorView();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}