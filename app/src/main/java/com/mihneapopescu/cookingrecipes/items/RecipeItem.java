package com.mihneapopescu.cookingrecipes.items;

public class RecipeItem {

    private String id;
    private String title;
    private String description;
    private String imageUrl;

    public RecipeItem(String id, String title, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description.substring(0, Math.min(description.length(), 150)) + (description.length() > 150 ? "..." : "");
        this.imageUrl = imageUrl;
    }

    public String getId() {return id;}

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