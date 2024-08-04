package com.example.charity_app;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button joinButton, heartButton, toyButton, feedButton, clothesButton, familyButton, litterButton;

    ImageButton heartIcon, bagIcon, accountIcon, menuIcon;

    FirebaseFirestore db;
    HashMap<String, String> projectDocumentIds = new HashMap<>();

    ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        joinButton = findViewById(R.id.joinButton);
        heartButton = findViewById(R.id.heartButton);
        toyButton = findViewById(R.id.toyButton);
        feedButton = findViewById(R.id.feedButton);
        clothesButton = findViewById(R.id.clothesButton);
        familyButton = findViewById(R.id.familyButton);
        litterButton = findViewById(R.id.litterButton);
        RelativeLayout firstWidget = findViewById(R.id.firstWidget);
        heartIcon = findViewById(R.id.heartIcon);
        menuIcon = findViewById(R.id.menuIcon);
        bagIcon = findViewById(R.id.bagIcon);
        accountIcon = findViewById(R.id.accountIcon);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);


        ImageButton menuIcon = findViewById(R.id.menuIcon);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        initializeProjectDocumentIds(db);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is logged in, hide the "Join Us" button
            joinButton.setVisibility(View.GONE);

            // Update firstWidget to show a "Thank you" message
            updateWidgetForLoggedInUser(firstWidget);
        } else {
            // User is not logged in, set the click listener for the "Join Us" button
            joinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to the registration screen
                    Intent intent = new Intent(MainActivity.this, Register.class);
                    startActivity(intent);
                }
            });
        }

        accountIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // User is logged in, open the Profile activity
                    Intent profileIntent = new Intent(MainActivity.this, Profile.class);
                    startActivity(profileIntent);
                } else {
                    // User is not logged in, open the Login activity
                    Intent loginIntent = new Intent(MainActivity.this, Login.class);
                    startActivity(loginIntent);
                }
            }
        });


        // Set up the ActionBarDrawerToggle
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        // Add a drawer listener to handle events
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        // Enable the hamburger icon for the ActionBarDrawerToggle
        actionBarDrawerToggle.syncState();

        // Set a click listener for the menu icon
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the drawer when the menu icon is clicked
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        //Side menu implementation
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_project_heart) {
                startDonateActivity(getString(R.string.heart), R.drawable.charitypink, "hearts");
                return true;
            } else if (item.getItemId() == R.id.nav_project_toy) {
                startDonateActivity(getString(R.string.toy), R.drawable.image_widget, "toys");
                return true;
            } else if (item.getItemId() == R.id.nav_project_feed) {
                startDonateActivity(getString(R.string.feed), R.drawable.feed, "feed");
                return true;
            } else if (item.getItemId() == R.id.nav_project_clothing) {
                startDonateActivity(getString(R.string.clothes), R.drawable.clothing, "clothing");
                return true;
            } else if (item.getItemId() == R.id.nav_project_family) {
                startDonateActivity(getString(R.string.family_project), R.drawable.family_health, "family");
                return true;
            } else if (item.getItemId() == R.id.nav_project_litter) {
                startDonateActivity(getString(R.string.litter_project), R.drawable.litter, "litter");
                return true;
            }
            return false;
        });


        bagIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent bagIntent = new Intent(MainActivity.this, BagActivity.class);
                    startActivity(bagIntent);
                } else {
                    Intent loginIntent = new Intent(MainActivity.this, Login.class);
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
                    Intent wishlistIntent = new Intent(MainActivity.this, WishlistActivity.class);
                    startActivity(wishlistIntent);
                } else {
                    // User is not logged in, open the Login activity
                    Intent loginIntent = new Intent(MainActivity.this, Login.class);
                    startActivity(loginIntent);
                }
            }
        });
        // Additional initialization and setup code...
        setButtonListeners();
    }

    private void initializeProjectDocumentIds(FirebaseFirestore db) {
        db.collection("projects2")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening for project documents: ", error);
                        return;
                    }

                    if (value != null) {
                        Log.d(TAG, "Fetching project documents...");

                        for (QueryDocumentSnapshot doc : value) {
                            // Use "projectName" as the key instead of document ID
                            String projectName = doc.getString("projectName");

                            if (projectName != null) {
                                projectDocumentIds.put(projectName.toLowerCase(), projectName);
                                Log.d(TAG, "Added to projectDocumentIds: " + projectName.toLowerCase() + " -> " + projectName);

                                // Additional logging to check if the document with projectName exists
                                db.collection("projects2")
                                        .whereEqualTo("projectName", projectName)
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                Log.d(TAG, "Document exists for projectName: " + projectName);
                                            } else {
                                                Log.e(TAG, "Document does not exist for projectName: " + projectName);
                                            }
                                        });
                            } else {
                                Log.e(TAG, "Error: Project Name is null for document with ID: " + doc.getId());
                            }
                        }
                    } else {
                        Log.e(TAG, "No documents found.");
                    }
                });
    }

    private void setButtonListeners() {
        // Set button listeners using the initialized project IDs
        Log.d(TAG, "Setting button listeners...");

        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDonateActivity(getString(R.string.heart), R.drawable.charitypink, "hearts");
            }
        });

        toyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDonateActivity(getString(R.string.toy), R.drawable.image_widget, "toys");
            }
        });

        feedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDonateActivity(getString(R.string.feed), R.drawable.feed, "feed");
            }
        });

        clothesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDonateActivity(getString(R.string.clothes), R.drawable.clothing, "clothes");
            }
        });

        familyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDonateActivity(getString(R.string.family_project), R.drawable.family_health, "family");
            }
        });

        litterButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDonateActivity(getString(R.string.litter_project), R.drawable.litter, "litter");
            }
        }));
    }

    private void startDonateActivity(String donateText, int imageResource, String projectName) {
        Log.d(TAG, "Starting Donate Activity. Project Name: " + projectName);

        // Check if the project name exists in the projectDocumentIds map
        if (projectDocumentIds.containsKey(projectName.toLowerCase())) {
            String projectKey = projectDocumentIds.get(projectName.toLowerCase());

            Intent intent = new Intent(MainActivity.this, Donate.class);
            intent.putExtra("textDonate", donateText);
            intent.putExtra("imageResource", imageResource);
            intent.putExtra("projectName", projectName);
            startActivity(intent);
        } else {
            Log.e(TAG, "Error: Project Name not found in projectDocumentIds. Project Name: " + projectName);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWidgetForLoggedInUser(RelativeLayout widget1) {
        // Remove the ImageView
        ImageView heartImageView = widget1.findViewById(R.id.heartImageView);
        widget1.removeView(heartImageView);

        // Center the TextView
        TextView donateText = widget1.findViewById(R.id.donateText);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) donateText.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        donateText.setLayoutParams(params);

        // Set the "Thank you" message
        donateText.setText("Thank you for making a difference!");
    }
}
