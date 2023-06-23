package com.mihneapopescu.cookingrecipes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.mihneapopescu.cookingrecipes.R;
import com.mihneapopescu.cookingrecipes.adapters.ReviewItemAdapter;
import com.mihneapopescu.cookingrecipes.models.Review;

import java.util.ArrayList;

import io.realm.Realm;

public class ReviewListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ReviewItemAdapter reviewItemAdapter;
    private Realm realm;

    public ReviewListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        realm = Realm.getDefaultInstance();

        ArrayList<Review> reviewList = new ArrayList<>(realm.copyFromRealm(realm.where(Review.class).equalTo("email", FirebaseAuth.getInstance().getCurrentUser().getEmail()).findAll()));

        // Check if the list is not null before setting the adapter
        if (reviewList != null) {
            recyclerView = view.findViewById(R.id.reviewsRecyclerView);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

            recyclerView.setLayoutManager(linearLayoutManager);

            // Set divider
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(view.getContext(), linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);

            reviewItemAdapter = new ReviewItemAdapter(reviewList);
            recyclerView.setAdapter(reviewItemAdapter);
        }
    }
}
