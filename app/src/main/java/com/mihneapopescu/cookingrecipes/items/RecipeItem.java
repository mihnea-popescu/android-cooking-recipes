package com.mihneapopescu.cookingrecipes.items;

import android.graphics.drawable.Drawable;

public class RecipeItem {

    private String title;
    private String description;
    private String imageUrl;

    public RecipeItem(String title, String description, String imageUrl) {
        this.title = title;
        this.description = description.substring(0, Math.min(description.length(), 150)) + (description.length() > 150 ? "..." : "");
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}