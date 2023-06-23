package com.mihneapopescu.cookingrecipes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mihneapopescu.cookingrecipes.MainActivity;
import com.mihneapopescu.cookingrecipes.R;
import com.mihneapopescu.cookingrecipes.adapters.RecipeItemAdapter;
import com.mihneapopescu.cookingrecipes.items.RecipeItem;
import com.mihneapopescu.cookingrecipes.models.Recipe;

import java.util.ArrayList;

import io.realm.Realm;

public class RecipeListFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecipeItemAdapter recipeItemAdapter;
    private Realm realm;

    public RecipeListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        realm = Realm.getDefaultInstance();

        ArrayList<Recipe> recipeList = new ArrayList<>(realm.copyFromRealm(realm.where(Recipe.class).findAll()));

        // Check if the list is not null before setting the adapter
        if (recipeList != null) {
            recyclerView = view.findViewById(R.id.recyclerView);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

            recyclerView.setLayoutManager(linearLayoutManager);

            // Set divider
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(view.getContext(), linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);

            showRecipeList(recipeList);
        }
    }

    private void showRecipeList(ArrayList<Recipe> recipes) {
        ArrayList<RecipeItem> recipeList = new ArrayList<RecipeItem>();

        for(Recipe recipe : recipes) {
            recipeList.add(new RecipeItem(recipe.getId(), recipe.getName(), recipe.getDescription(), recipe.getPhotoUrl()));
        }

        recipeItemAdapter = new RecipeItemAdapter(recipeList, getActivity());
        recyclerView.setAdapter(recipeItemAdapter);
    }
}
