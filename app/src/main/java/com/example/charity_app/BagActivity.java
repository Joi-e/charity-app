package com.example.charity_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import com.example.charity_app.BagItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import com.example.charity_app.BagItem;

public class BagActivity extends AppCompatActivity {
    private ListView bagListView;
    private BagAdapter bagAdapter;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bag_layout);

        // Initialize the ListView
        bagListView = findViewById(R.id.bagListView);

        // Dummy data for testing
        bagAdapter = new BagAdapter(this, new ArrayList<>(), userId); // Pass an empty list initially
        bagListView.setAdapter(bagAdapter);

        // Fetch user's bag items from Firestore
        fetchUserBagItems();

        // Set item click listener to handle "Pay" button for each item
        bagListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BagItem clickedItem = (BagItem) parent.getItemAtPosition(position);
                navigateToPaymentActivity(clickedItem);
            }
        });

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

    private void fetchUserBagItems() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("userBags").document(userId)
                    .collection("bagItems")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<BagItem> userBagItems = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Convert Firestore document to BagItem
                                BagItem bagItem = document.toObject(BagItem.class);

                                // Set the projectId manually based on the logic you provided
                                String projectId = getManualProjectId(bagItem.getDocumentId());
                                bagItem.setProjectId(projectId);

                                userBagItems.add(bagItem);
                            }

                            // Update the adapter with user's bag items
                            bagAdapter.updateData(userBagItems);
                        } else {
                            Log.e("BagActivity", "Error getting user bag items: ", task.getException());
                            // Handle the error, show a message, or retry
                        }
                    });
        } else {
        }
    }

    private String getManualProjectId(String projectName) {
        // Map projectName to projectId manually
        switch (projectName) {
            case "hearts":
                return "Hearts Project";
            case "toys":
                return "Project Toys";
            case "litter":
                return "Project Litter";
            case "family":
                return "Project Family";
            case "feed":
                return "Project Food";
            case "clothes":
                return "Project Clothes";
            default:
                return "null";
        }
    }

    private void navigateToPaymentActivity(BagItem bagItem) {
        // Use the document ID as the project ID
        String projectId = bagItem.getDocumentId();

        // Start the Payment activity with details of the selected item
        Intent paymentIntent = new Intent(BagActivity.this, Payment.class);
        paymentIntent.putExtra("projectName", bagItem.getDocumentId());  // Use "projectName" here
        paymentIntent.putExtra("donationAmount", bagItem.getDonationAmount());
        startActivity(paymentIntent);
    }
}
