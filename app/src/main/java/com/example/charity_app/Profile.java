package com.example.charity_app;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    ImageButton heartIcon, bagIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_details);

        heartIcon = findViewById(R.id.heartIcon);
        bagIcon = findViewById(R.id.bagIcon);

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Inside the onCreate method of Profile class

        if (currentUser != null) {
            // Populate UI elements with current user details
            EditText editFullName = findViewById(R.id.editFullName);
            EditText editEmail = findViewById(R.id.editEmail);

            // Retrieve user details from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            editFullName.setText(fullName);

                            // You can also set other user details like email, etc.
                            String email = documentSnapshot.getString("email");
                            editEmail.setText(email);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting user details from Firestore", e);
                    });

            // Display username
            TextView userName = findViewById(R.id.userName);
            userName.setText("Username: " + currentUser.getUid());
        }


        Button buttonSave = findViewById(R.id.buttonSave);
        Button logoutButton = findViewById(R.id.logoutButton);
        TextView changePasswordTextView = findViewById(R.id.changePasswordTextView);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call a method to handle saving updated details
                updateDetails();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call a method to handle logout
                logout();
            }
        });

        changePasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the ChangePasswordActivity
                Intent intent = new Intent(Profile.this, ChangePassword.class);
                startActivity(intent);
            }
        });

        bagIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent bagIntent = new Intent(Profile.this, BagActivity.class);
                    startActivity(bagIntent);
                } else {
                    Intent loginIntent = new Intent(Profile.this, Login.class);
                    startActivity(loginIntent);
                }
            }
        });

        heartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // User is logged in, open the WishlistActivity
                    Intent wishlistIntent = new Intent(Profile.this, WishlistActivity.class);
                    startActivity(wishlistIntent);
                } else {
                    // User is not logged in, open the Login activity
                    Intent loginIntent = new Intent(Profile.this, Login.class);
                    startActivity(loginIntent);
                }
            }
        });
    }

    private void updateDetails() {
        EditText editFullName = findViewById(R.id.editFullName);
        String newFullName = editFullName.getText().toString().trim();

        // Check if the full name has changed
        if (!newFullName.equals(currentUser.getDisplayName())) {
            // Update user's display name
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newFullName)
                    .build();

            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User display name updated.");
                                // You can also update the email if needed:
                                // currentUser.updateEmail(email);
                                Toast.makeText(Profile.this, "Full name updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "Error updating user display name.", task.getException());
                                Toast.makeText(Profile.this, "Error updating full name", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // The full name has not changed
            Toast.makeText(Profile.this, "No changes made", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();

        // Redirect to LoginActivity or any other appropriate activity
        Intent intent = new Intent(Profile.this, Login.class);
        startActivity(intent);
        finish();
    }


}
