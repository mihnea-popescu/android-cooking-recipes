package com.mihneapopescu.cookingrecipes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.mihneapopescu.cookingrecipes.adapters.RecipeItemAdapter;
import com.mihneapopescu.cookingrecipes.models.RecipeItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecipeItemAdapter adapter;
    private List<RecipeItem> recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);

        // Set divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        recipeList = new ArrayList<>();

        // Add recipes to the list
        recipeList.add(new RecipeItem("Chicken Biryani", "A spicy and aromatic dish made with basmati rice and chicken", R.drawable.ic_launcher_background));
        recipeList.add(new RecipeItem("Sarmale", "Bune și gustoase", R.drawable.ic_launcher_background));
        recipeList.add(new RecipeItem("Shaorma", "Cu de toate", R.drawable.ic_launcher_background));

        adapter = new RecipeItemAdapter(recipeList);
        recyclerView.setAdapter(adapter);
    }
}