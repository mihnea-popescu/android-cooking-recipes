package com.mihneapopescu.cookingrecipes;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mihneapopescu.cookingrecipes.adapters.IngredientItemAdapter;
import com.mihneapopescu.cookingrecipes.models.Recipe;

import io.realm.Realm;

public class RecipeDetailActivity extends AppCompatActivity {
    private WebView youtubePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        String recipeId = getIntent().getStringExtra("RECIPE_ID");

        Realm realm = Realm.getDefaultInstance();
        Recipe recipe = realm.where(Recipe.class).equalTo("id", recipeId).findFirst();

        // Populate the views with the recipe details
        TextView nameTextView = findViewById(R.id.nameTextView);
        ImageView imageView = findViewById(R.id.imageView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);

        // Set the contents
        nameTextView.setText(recipe.getName());
        descriptionTextView.setText(recipe.getDescription());

        // Load image
        Glide.with(this).load(recipe.getPhotoUrl()).into(imageView);

        // Create ingredients Recycler View
        RecyclerView recyclerView = findViewById(R.id.ingredientsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        IngredientItemAdapter ingredientItemAdapter = new IngredientItemAdapter(recipe.getIngredients());
        recyclerView.setAdapter(ingredientItemAdapter);

        // Create the youtube embedded player
        String youtubeUrl = recipe.getYoutubeUrl();

        if(youtubeUrl.length() > 0) {
            findViewById(R.id.youtubePlayerTitle).setVisibility(View.VISIBLE);

            youtubePlayer = findViewById(R.id.youtubePlayer);
            youtubePlayer.getSettings().setJavaScriptEnabled(true);

            String videoId = youtubeUrl.split("v=")[1];

            youtubePlayer.loadData("<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/" + videoId + "\" frameborder=\"0\"></iframe>", "text/html" , "utf-8" );
        }
    }
}
