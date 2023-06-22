package com.mihneapopescu.cookingrecipes.services.interfaces;

public interface ImgurUploadCallback {
    void onSuccess(String url);
    void onError(Exception e);
}
