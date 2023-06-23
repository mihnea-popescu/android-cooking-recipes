package com.mihneapopescu.cookingrecipes;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mihneapopescu.cookingrecipes.adapters.IngredientItemAdapter;
import com.mihneapopescu.cookingrecipes.adapters.ReviewItemAdapter;
import com.mihneapopescu.cookingrecipes.services.helpers.BitmapToUri;
import com.mihneapopescu.cookingrecipes.services.interfaces.ImgurUploadCallback;
import com.mihneapopescu.cookingrecipes.models.Recipe;
import com.mihneapopescu.cookingrecipes.models.Review;
import com.mihneapopescu.cookingrecipes.api.ImgurUpload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.Realm;

public class RecipeDetailActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Realm realm;
    private Recipe recipe;

    private RecyclerView reviewList;

    private WebView youtubePlayer;

    // Reviews
    private EditText reviewMessageEditText;
    private ImageButton reviewPhotoButton;
    private Button submitReviewButton;
    private Uri selectedImageUri;

    // Request code for image chooser intent
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        String recipeId = getIntent().getStringExtra("RECIPE_ID");

        realm = Realm.getDefaultInstance();
        recipe = realm.where(Recipe.class).equalTo("id", recipeId).findFirst();

        // Populate the views with the recipe details
        TextView nameTextView = findViewById(R.id.nameTextView);
        ImageView imageView = findViewById(R.id.imageView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);

        // Set the contents
        nameTextView.setText(recipe.getName());
        descriptionTextView.setText(recipe.getDescription());

        // Load image
        Glide.with(this).load(recipe.getPhotoUrl()).into(imageView);

        // Create ingredients Recycler View
        RecyclerView recyclerView = findViewById(R.id.ingredientsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        IngredientItemAdapter ingredientItemAdapter = new IngredientItemAdapter(recipe.getIngredients());
        recyclerView.setAdapter(ingredientItemAdapter);

        // Create the youtube embedded player
        String youtubeUrl = recipe.getYoutubeUrl();

        if(youtubeUrl.length() > 0) {
            findViewById(R.id.youtubePlayerTitle).setVisibility(View.VISIBLE);

            youtubePlayer = findViewById(R.id.youtubePlayer);
            youtubePlayer.getSettings().setJavaScriptEnabled(true);

            String videoId = youtubeUrl.split("v=")[1];

            youtubePlayer.loadData("<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/" + videoId + "\" frameborder=\"0\"></iframe>", "text/html" , "utf-8" );
        }

        // Initialize review list
        reviewList = findViewById(R.id.reviewsRecyclerView);
        fetchReviews();

        // Initialize review form
        reviewMessageEditText = findViewById(R.id.review_message);
        reviewPhotoButton = findViewById(R.id.review_photo_button);
        submitReviewButton = findViewById(R.id.submit_review_button);

        reviewPhotoButton.setOnClickListener(v -> selectImage());

        submitReviewButton.setOnClickListener(v -> {
            String reviewMessage = reviewMessageEditText.getText().toString();
            submitReview(reviewMessage, selectedImageUri);
        });
    }

    private void fetchReviews() {
        // Fetch reviews
        List<Review> reviews = realm.copyFromRealm(recipe.getReviews());

        if(!reviews.isEmpty()) {
            findViewById(R.id.reviewsTitle).setVisibility(View.VISIBLE);
        }

        ReviewItemAdapter reviewItemAdapter = new ReviewItemAdapter(reviews);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        reviewList.setLayoutManager(linearLayoutManager);

        // Set divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
        reviewList.addItemDecoration(dividerItemDecoration);

        reviewList.setAdapter(reviewItemAdapter);
    }

    private void submitReview(String reviewMessage, Uri imageUri) {
        String message = reviewMessage.trim();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        if (message.isEmpty() && selectedImageUri == null) {
            Toast.makeText(RecipeDetailActivity.this, "Please enter a message or select a photo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() < 5 && !message.isEmpty()) {
            reviewMessageEditText.setError("Please enter a valid review of at least 5 characters");
            return;
        }

        AtomicReference<Review> review = new AtomicReference<Review>();

        realm.executeTransaction(realm -> {
            review.set(realm.createObject(Review.class, UUID.randomUUID().toString()));

            if(!message.isEmpty()) {
                review.get().setMessage(message);
            }

            review.get().setRecipe(recipe);
            review.get().setEmail(email);
        });

        String reviewId = review.get().getId();

        if(imageUri != null) {
            // Verify image permissions
            // Check if the permission is already granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted, so request it
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }

            new ImgurUpload(this, imageUri).submit(new ImgurUploadCallback() {
                @Override
                public void onSuccess(String url) {
                    saveReview(reviewId, url);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(RecipeDetailActivity.this, "Failed to upload photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            new Thread(() -> {
                saveReview(reviewId, "");
            }).start();
        }
    }

    public void saveReview(String reviewId, String photoUrl) {
        Realm realm = Realm.getDefaultInstance();

        Review review = realm.where(Review.class).equalTo("id", reviewId).findFirst();
        Recipe recipe = review.getRecipe();

        // Add review to recipe review list
        realm.executeTransaction(r -> {
            if(photoUrl != "") {
                review.setPhotoUrl(photoUrl);
            }

            recipe.getReviews().add(review);
        });

        // Store review in firestore collection
        Map<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("id", review.getId());
        reviewMap.put("email", review.getEmail());
        reviewMap.put("message", review.getMessage());
        reviewMap.put("photoUrl", review.getPhotoUrl());

        db.collection("recipes").document(recipe.getId())
                .collection("reviews").document(review.getId())
                .set(reviewMap)
                .addOnCompleteListener(aVoid -> {
                    Toast.makeText(RecipeDetailActivity.this, "Review Submitted!", Toast.LENGTH_SHORT).show();
                    reviewMessageEditText.setText("");
                    selectedImageUri = null;
                    reviewMessageEditText.clearFocus();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.hideKeyboard(RecipeDetailActivity.this);
                        }
                    }, 500);

                    // Reload data
                    fetchReviews();

        }).addOnFailureListener(e -> Log.w("RECIPES", "Error adding review to recipe", e));

        realm.close();
    }

    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDetailActivity.this);
        builder.setTitle("Add Photo:");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    mTakePicture.launch(null);
                } else if (options[item].equals("Choose from Gallery")) {
                    mGetContent.launch("image/*");
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private final ActivityResultLauncher<Void> mTakePicture= registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            new ActivityResultCallback<Bitmap>() {
                @Override
                public void onActivityResult(Bitmap bitmap) {
                    // Handle the returned Uri
                    selectedImageUri = BitmapToUri.process(bitmap, RecipeDetailActivity.this.getApplicationContext());
                }
            });

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    selectedImageUri = uri;
                }
            });

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Toast.makeText(this, "Storage permission is needed to access photos", Toast.LENGTH_SHORT).show();
                    }
                }
                return;
            }
        }
    }
}
