package com.example.charity_app;

import static com.example.charity_app.FirebaseFunction.addToBagAndUpdateProject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Donate extends AppCompatActivity {

    private String projectName;
    ImageButton heartIcon, bagIcon, accountIcon, menuIcon;
    ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_details);

        menuIcon = findViewById(R.id.menuIcon);
        heartIcon = findViewById(R.id.heartIcon);
        accountIcon = findViewById(R.id.accountIcon);
        bagIcon = findViewById(R.id.bagIcon);

        String donateText = getIntent().getStringExtra("textDonate");
        projectName = getIntent().getStringExtra("projectName");
        int imageResource = getIntent().getIntExtra("imageResource", R.drawable.image_widget);

        TextView textDonate = findViewById(R.id.textDonate);
        ImageView widgetImage = findViewById(R.id.widgetImage);

        textDonate.setText(donateText);
        widgetImage.setImageResource(imageResource);

        Button donateButton = findViewById(R.id.donateButton);
        ImageButton arrowButton = findViewById(R.id.arrowButton);
        ImageButton heartButton = findViewById(R.id.heartButton);

        heartButton.setOnClickListener(v -> {
            if (isLoggedIn()) {
                Log.d("Donate", "Clicked Heart button. Project Name: " + projectName);
                addToWishlist(projectName);
                Toast.makeText(Donate.this, "Added to Wishlist", Toast.LENGTH_SHORT).show();
            } else {
                Intent loginIntent = new Intent(Donate.this, Login.class);
                startActivity(loginIntent);
            }
        });

        arrowButton.setOnClickListener(view -> onBackPressed());

        donateButton.setOnClickListener(v -> {
            if (isLoggedIn()) {
                Log.d("Donate", "Clicked Donate button. Project Name: " + projectName);
                fetchProjectName(projectName);
            } else {
                Intent loginIntent = new Intent(Donate.this, Login.class);
                startActivity(loginIntent);
            }
        });

        accountIcon.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Intent profileIntent = new Intent(Donate.this, Profile.class);
                startActivity(profileIntent);
            } else {
                Intent loginIntent = new Intent(Donate.this, Login.class);
                startActivity(loginIntent);
            }
        });

        bagIcon.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Intent bagIntent = new Intent(Donate.this, BagActivity.class);
                startActivity(bagIntent);
            } else {
                Intent loginIntent = new Intent(Donate.this, Login.class);
                startActivity(loginIntent);
            }
        });

        heartIcon.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Intent wishlistIntent = new Intent(Donate.this, WishlistActivity.class);
                startActivity(wishlistIntent);
            } else {
                Intent loginIntent = new Intent(Donate.this, Login.class);
                startActivity(loginIntent);
            }
        });
    }

    private void fetchProjectName(String projectKey) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("projects2")
                    .whereEqualTo("projectName", projectKey)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                projectName = document.getString("projectName");
                                Log.d("Donate", "Fetched Project Name: " + projectName);
                                EditText donateAmountEditText = findViewById(R.id.donateAmount);
                                String donationAmountString = donateAmountEditText.getText().toString();
                                double donationAmount = Double.parseDouble(donationAmountString);
                                final Double finalDonationAmount = donationAmount;

                                if (projectName != null) {
                                    addToBagAndUpdateProject(userId, projectName, finalDonationAmount);
                                    showDonationOptionsDialog(userId, projectName, finalDonationAmount);
                                } else {
                                    Log.e("Donate", "projectName is null");
                                }
                            }
                        } else {
                            Log.e("Donate", "Error getting documents: ", task.getException());
                        }
                    });
        } else {
            Log.e("Donate", "User not logged in");
        }
    }

    private void showDonationOptionsDialog(String userId, String projectName, Double donationAmount) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(Donate.this);
            builder.setTitle("Donation Options")
                    .setMessage("Would you like to add to the bag or proceed to payment?")
                    .setPositiveButton("Add to Bag", (dialog, id) -> {
                        Log.d("Donate", "Add to Bag clicked");
                        addToBagAndUpdateProject(userId, projectName, donationAmount);
                        Intent bagActivityIntent = new Intent(Donate.this, BagActivity.class);
                        bagActivityIntent.putExtra("projectName", projectName);
                        bagActivityIntent.putExtra("donationAmount", donationAmount);
                        startActivity(bagActivityIntent);
                    })
                    .setNegativeButton("Proceed to Payment", (dialog, id) -> {
                        Log.d("Donate", "Proceed to Payment clicked");
                        Log.d("Donate", "donationAmount before launching Payment: " + donationAmount);
                        Intent paymentIntent = new Intent(Donate.this, Payment.class);
                        paymentIntent.putExtra("projectName", projectName);
                        paymentIntent.putExtra("donationAmount", String.valueOf(donationAmount));
                        startActivity(paymentIntent);
                    })
                    .setNeutralButton("Cancel", (dialog, id) -> {
                        Log.d("Donate", "Cancel clicked");
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e("Donate", "Error showing dialog: " + e.getMessage());
        }
    }

    private void addToBag(String projectId, double donationAmount) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFunction.addToBag(userId, projectId, donationAmount);
        } else {
            // User is not logged in, handle accordingly
        }
    }

    private void addToWishlist(String projectId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFunction.addToWishlist(userId, projectId);
        } else {
            Intent loginIntent = new Intent(Donate.this, Login.class);
            startActivity(loginIntent);
        }
    }

    private boolean isLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

