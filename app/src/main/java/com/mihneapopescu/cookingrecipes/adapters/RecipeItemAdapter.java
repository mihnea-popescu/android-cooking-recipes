package com.mihneapopescu.cookingrecipes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.mihneapopescu.cookingrecipes.R;
import com.mihneapopescu.cookingrecipes.models.RecipeItem;

import java.util.List;

public class RecipeItemAdapter extends RecyclerView.Adapter<RecipeItemAdapter.ViewHolder> {

    private List<RecipeItem> recipeList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView recipeImage;
        public TextView recipeTitle;
        public TextView recipeDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            recipeDescription = itemView.findViewById(R.id.recipe_description);
        }
    }

    public RecipeItemAdapter(List<RecipeItem> recipeList) {
        this.recipeList = recipeList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item_menu, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RecipeItem recipe = recipeList.get(position);
        holder.recipeTitle.setText(recipe.getTitle());
        holder.recipeDescription.setText(recipe.getDescription());
        holder.recipeImage.setImageResource(recipe.getImageId());
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }
}
