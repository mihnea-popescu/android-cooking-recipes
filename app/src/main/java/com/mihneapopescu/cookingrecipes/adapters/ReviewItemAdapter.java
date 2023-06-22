package com.mihneapopescu.cookingrecipes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mihneapopescu.cookingrecipes.R;
import com.mihneapopescu.cookingrecipes.models.Review;

import java.util.List;

public class ReviewItemAdapter extends RecyclerView.Adapter<ReviewItemAdapter.ReviewViewHolder> {
    private List<Review> reviews;

    public ReviewItemAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.email.setText(review.getEmail());
        holder.message.setText(review.getMessage());

        // Assuming you use Glide or Picasso to handle image loading
        // You can handle this part based on your preference
        if(review.getPhotoUrl() != "") {
            holder.photo.setVisibility(View.VISIBLE);

            Glide.with(holder.itemView.getContext())
                    .load(review.getPhotoUrl())
                    .into(holder.photo);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView email;
        ImageView photo;
        TextView message;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            email = itemView.findViewById(R.id.email);
            photo = itemView.findViewById(R.id.photo);
            message = itemView.findViewById(R.id.message);
        }
    }
}
