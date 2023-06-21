package com.mihneapopescu.cookingrecipes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihneapopescu.cookingrecipes.R;
import com.mihneapopescu.cookingrecipes.models.Ingredient;

import java.util.List;

public class IngredientItemAdapter extends RecyclerView.Adapter<IngredientItemAdapter.IngredientViewHolder>{
    private List<Ingredient> ingredientList;

    public IngredientItemAdapter(List<Ingredient> ingredientList) {
        this.ingredientList = ingredientList;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredient_item, parent, false);
        return new IngredientViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredientList.get(position);
        holder.nameTextView.setText(ingredient.getName());
        holder.quantityTextView.setText(ingredient.getQuantity());
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView quantityTextView;

        IngredientViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.ingredientNameTextView);
            quantityTextView = view.findViewById(R.id.ingredientQuantityTextView);
        }
    }
}
