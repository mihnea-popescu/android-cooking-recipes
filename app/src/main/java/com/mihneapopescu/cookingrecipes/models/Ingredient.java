package com.mihneapopescu.cookingrecipes.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Ingredient extends RealmObject {
    @PrimaryKey
    private String id;
    private String name;
    private String quantity;

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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
