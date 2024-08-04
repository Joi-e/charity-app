package com.example.charity_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private static final String TAG = "WishlistActivity";
    private List<WishlistItem> wishlistItems;
    private WishlistAdapter wishlistAdapter;

    Button donateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wishlist_layout);

        // Assuming you have a RecyclerView with the ID recyclerViewWishlist
        RecyclerView recyclerView = findViewById(R.id.wishlistRecyclerView);

        // Initialize the wishlist items (replace this with your logic)
        wishlistItems = new ArrayList<>();

        // Set up and bind the adapter
        wishlistAdapter = new WishlistAdapter(wishlistItems, this, new WishlistAdapter.OnDonateClickListener() {
            @Override
            public void onDonateClick(WishlistItem item) {
                // Handle the donate button click, start DonateActivity with relevant information
                startDonateActivity(item.getProjectId());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(wishlistAdapter);
        // Retrieve wishlist items asynchronously and update the adapter
        retrieveWishlistItems();

        // Initialize the backButton ImageView
        ImageView backButton = findViewById(R.id.backButton);

        // Set click listener for the backButton
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the previous activity
                onBackPressed();
            }
        });
    }

    // Replace this with your logic to retrieve wishlist items from Firebase or any other source
    private void retrieveWishlistItems() {
        // Get the current user's ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Access the user's wishlist collection in Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).collection("wishlist")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            wishlistItems.clear(); // Clear existing items
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Assuming the wishlist document has a field "projectId"
                                String projectId = document.getString("projectId");
                                if (projectId != null) {
                                    WishlistItem wishlistItem = new WishlistItem(projectId);
                                    wishlistItems.add(wishlistItem);
                                }
                            }

                            // Notify the adapter about the changes in the dataset
                            wishlistAdapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Error getting wishlist items", task.getException());
                        }
                    });
        }
    }

    // Add this method to handle the donate button click
    public void startDonateActivity(String projectId) {
        Intent intent = new Intent(WishlistActivity.this, Donate.class);

        // Set details based on projectId
        String donateText;
        int imageResource;

        switch (projectId) {
            case "hearts":
                donateText = getString(R.string.heart);
                imageResource = R.drawable.charitypink;
                break;
            case "toys":
                donateText = getString(R.string.toy);
                imageResource = R.drawable.image_widget;
                break;
            case "feed":
                donateText = getString(R.string.feed);
                imageResource = R.drawable.feed;
                break;
            case "clothing":
                donateText = getString(R.string.clothes);
                imageResource = R.drawable.clothing;
                break;
            case "family":
                donateText = getString(R.string.family_project);
                imageResource = R.drawable.family_health;
                break;
            case "litter":
                donateText = getString(R.string.litter_project);
                imageResource = R.drawable.litter;
                break;
            // Add more cases as needed

            default:
                donateText = "";
                imageResource = R.drawable.charitypink;  // Use a default image if needed
        }

        intent.putExtra("textDonate", donateText);
        intent.putExtra("imageResource", imageResource);
        intent.putExtra("projectName", projectId);
        startActivity(intent);
    }

}



