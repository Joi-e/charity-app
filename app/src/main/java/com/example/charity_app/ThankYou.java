package com.example.charity_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ThankYou extends AppCompatActivity {

    private FirebaseFirestore db;
    private ProgressBar progressBar;

    Button backButtton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thankyou_page);

        db = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        // Retrieve values from Intent
        String projectName = getIntent().getStringExtra("projectName");
        double goalAmount = getIntent().getDoubleExtra("goalAmount", 0.0);

        // Fetch current amount from Firebase
        fetchCurrentAmount(projectName, goalAmount);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the previous activity
                Intent homeIntent = new Intent(ThankYou.this, MainActivity.class);
                startActivity(homeIntent);
            }
        });
    }

    private void fetchCurrentAmount(String projectName, double initialGoalAmount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference projectRef = db.collection("projects2").document(projectName);

        projectRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve currentAmount from the document
                double currentAmount = documentSnapshot.getDouble("currentAmount");

                // Update the ProgressBar based on the progress
                updateProgressBar(currentAmount, initialGoalAmount);

                // Display the progress using initialGoalAmount
                displayProgress(currentAmount, initialGoalAmount);
            } else {
                // Handle the case where the project document does not exist
                Log.e("ThankYou", "Project document does not exist");
            }
        }).addOnFailureListener(e -> {
            // Handle failure while fetching the document
            Log.e("ThankYou", "Error fetching project data", e);
        });
    }

    private void updateProgressBar(double currentAmount, double goalAmount) {
        // Calculate progress percentage
        int progress = (int) ((currentAmount / goalAmount) * 100);

        // Set the progress to the ProgressBar
        progressBar.setProgress(progress);
    }

    private void displayProgress(double currentAmount, double goalAmount) {
        // Find TextView for project progress
        TextView projectProgressTextView = findViewById(R.id.projectProgress);

        // Set text dynamically
        String progressText = String.format("Current Amount: $%.2f\nGoal: $%.2f", currentAmount, goalAmount);
        projectProgressTextView.setText(progressText);
    }
}
