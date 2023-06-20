package com.mihneapopescu.cookingrecipes.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Recipe extends RealmObject {
    @PrimaryKey
    private String id;
    private String name;
    private String user;
    private String photoUrl;
    private String description;
    private String youtubeUrl;
    private RealmList<Ingredient> ingredients;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photo) {
        this.photoUrl = photo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public RealmList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(RealmList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
}
