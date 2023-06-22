package com.mihneapopescu.cookingrecipes.services;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.mihneapopescu.cookingrecipes.BuildConfig;
import com.mihneapopescu.cookingrecipes.services.interfaces.ImgurUploadCallback;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImgurUpload {
    private Uri imageUri;
    private Activity activity;

    public ImgurUpload(Activity activity, Uri imageUri) {
        this.activity = activity;
        this.imageUri = imageUri;
    }

    public void submit(ImgurUploadCallback callback) {
        new Thread(() -> {
            try {
                String clientId = BuildConfig.IMGUR_CLIENT_ID;

                URL url = new URL("https://api.imgur.com/3/image");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(activity.getContentResolver(), imageUri));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);


                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "Client-ID " + clientId);
                conn.setRequestMethod("POST");

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(encodedImage);
                wr.flush();

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                String response = "";

                while ((line = rd.readLine()) != null) {
                    response += line;
                }

                wr.close();
                rd.close();

                JSONObject json = new JSONObject(response);
                String resultUrl = json.getJSONObject("data").getString("link");

                callback.onSuccess(resultUrl);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
