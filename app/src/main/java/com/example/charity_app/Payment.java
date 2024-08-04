package com.example.charity_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Payment extends AppCompatActivity {

    private static final String TAG = "Payment";
    private String originalDonationAmount;
    private EditText donateAmountEditText;

    String projectName;

    private boolean isProjectDataFetched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_page);

        EditText cardNumberEditText = findViewById(R.id.cardNumber);
        InputFilter.LengthFilter cardNumberFilter = new InputFilter.LengthFilter(16);
        cardNumberEditText.setFilters(new InputFilter[]{cardNumberFilter});

        ImageButton arrowButton = findViewById(R.id.arrowButton);
        donateAmountEditText = findViewById(R.id.donateAmount);

        arrowButton.setOnClickListener(view -> onBackPressed());

        boolean fromBagActivity = getIntent().getBooleanExtra("fromBagActivity", false);

        if (fromBagActivity) {
            BagItem bagItem = getIntent().getParcelableExtra("bagItem");
            if (bagItem != null) {
                donateAmountEditText.setText(String.valueOf(bagItem.getDonationAmount()));
                projectName = bagItem.getDocumentId();
                getIntent().putExtra("projectName", bagItem.getDocumentId());
            }
        } else {
            originalDonationAmount = getIntent().getStringExtra("donationAmount");
            projectName = getIntent().getStringExtra("projectName");
            Log.d(TAG, "Payment Activity Started. Project Name: " + projectName);

            Log.d(TAG, "originalDonationAmount in Payment: " + originalDonationAmount);

            if (originalDonationAmount != null) {
                donateAmountEditText.setText(originalDonationAmount);
            } else {
                donateAmountEditText.setText("0.0");
            }
        }

        Button submitPaymentButton = findViewById(R.id.submitButton);

        submitPaymentButton.setOnClickListener(v -> {
            String editedDonationAmount = donateAmountEditText.getText().toString();
            String cardNumber = cardNumberEditText.getText().toString();

            if (cardNumber.length() == 16) {
                updateProjectData(projectName, editedDonationAmount);
            } else {
                cardNumberEditText.setError("Card number must be 16 digits");
            }
        });
    }

    private void updateProjectData(String projectName, String editedDonationAmount) {
        Log.d("Payment", "projectName: " + projectName);

        if (projectName == null) {
            Log.e("Firestore", "projectName is null");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference projectRef = db.collection("projects2").document(projectName);

        double donationAmountValue = Double.parseDouble(editedDonationAmount);

        HashMap<String, Object> data = new HashMap<>();
        data.put("currentAmount", FieldValue.increment(donationAmountValue));

        projectRef.update(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Payment", "Successfully updated project data. Donation Amount: " + donationAmountValue);
                    fetchUpdatedProjectData(projectName, donationAmountValue);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating project data", e);
                });
    }

    private void fetchUpdatedProjectData(String projectName, double donationAmount) {
        if (isProjectDataFetched) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference projectRef = db.collection("projects2").document(projectName);

        projectRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d("Payment", "Fetched Document Data: " + documentSnapshot.getData());

                if (documentSnapshot.contains("goalAmount")) {
                    Double goalAmount = documentSnapshot.getDouble("goalAmount");
                    if (goalAmount != null) {
                        navigateToThankYouPage(projectName, donationAmount, goalAmount);
                        isProjectDataFetched = true;
                    } else {
                        Log.e("Payment", "goalAmount is null for Project Name: " + projectName);
                    }
                } else {
                    Log.e("Payment", "goalAmount field does not exist for Project Name: " + projectName);
                }
            } else {
                Log.e("Payment", "Updated project document does not exist. Project Name: " + projectName);
            }
        }).addOnFailureListener(e -> {
            Log.e("Payment", "Error fetching updated project data. Project Name: " + projectName, e);
        });
    }

    private void navigateToThankYouPage(String projectId, double donationAmount, double goalAmount) {
        Intent thankYouIntent = new Intent(Payment.this, ThankYou.class);
        thankYouIntent.putExtra("projectName", projectName);
        thankYouIntent.putExtra("donationAmount", donationAmount);
        thankYouIntent.putExtra("goalAmount", goalAmount);
        startActivity(thankYouIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

