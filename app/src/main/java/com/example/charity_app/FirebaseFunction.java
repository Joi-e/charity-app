package com.example.charity_app;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseFunction {

    public static void addToBag(String userId, String projectId, double donationAmount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        int imageResource = getManualImageResource(projectId);

        DocumentReference bagItemRef = db.collection("userBags").document(userId)
                .collection("bagItems").document(projectId);

        final double finalDonationAmount = donationAmount;

        bagItemRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot innerDocument = task.getResult();
                if (innerDocument.exists()) {
                    double existingDonationAmount = innerDocument.getDouble("donationAmount");
                    double updatedDonationAmount = finalDonationAmount + existingDonationAmount;
                    bagItemRef.update("donationAmount", updatedDonationAmount);
                } else {
                    BagItem bagItem = new BagItem(projectId, finalDonationAmount, imageResource);

                    db.collection("userBags").document(userId)
                            .collection("bagItems").document(projectId)
                            .set(bagItem)
                            .addOnSuccessListener(aVoid -> {
                                // Successfully added to cart
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseFunction", "Error adding to cart", e);
                            });
                }
            } else {
                Log.e("FirebaseFunction", "Error checking bag item existence: ", task.getException());
            }
        });
    }

    public static int getManualImageResource(String projectId) {
        Log.d("FirebaseFunction", "getManualImageResource - projectId: " + projectId);
        switch (projectId) {
            case "Project Heart":
                return R.drawable.charitypink;
            case "Project Toys":
                return R.drawable.toybox;
            case "Project Litter":
                return R.drawable.litter;
            case "Project Family":
                return R.drawable.family_health;
            case "Project Food":
                return R.drawable.feed;
            case "Project Clothes":
                return R.drawable.clothing;
            default:
                return R.drawable.charitypink;
        }
    }

    public static void updateCurrentAmount(String projectName, double donationAmount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("projects2")
                .whereEqualTo("projectName", projectName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String projectId = document.getId();
                            DocumentReference projectRef = db.collection("projects2").document(projectId);

                            db.runTransaction(transaction -> {
                                DocumentSnapshot projectSnapshot = transaction.get(projectRef);
                                if (projectSnapshot.exists()) {
                                    double currentAmount = projectSnapshot.getDouble("currentAmount") + donationAmount;
                                    transaction.update(projectRef, "currentAmount", currentAmount);
                                } else {
                                    Log.e("Firestore", "Project document does not exist for projectId: " + projectId);
                                }
                                return null;
                            }).addOnSuccessListener(aVoid -> {
                                Log.d("Firestore", "CurrentAmount updated successfully for projectId: " + projectId);
                            }).addOnFailureListener(e -> {
                                Log.e("Firestore", "Error updating currentAmount for projectId: " + projectId, e);
                            });
                        }
                    } else {
                        Log.e("FirebaseFunction", "Error fetching project: ", task.getException());
                    }
                });
    }

    public static void addToBagAndUpdateProject(String userId, String projectName, double donationAmount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("projects2")
                .whereEqualTo("projectName", projectName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String projectId = document.getId();
                            addToBag(userId, projectId, donationAmount);
                            updateCurrentAmount(projectId, donationAmount);
                        }
                    } else {
                        Log.e("FirebaseFunction", "Error fetching project: ", task.getException());
                    }
                });
    }

    public static void removeFromBag(String userId, String projectId, BagAdapter bagAdapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference bagItemRef = db.collection("userBags").document(userId)
                .collection("bagItems").document(projectId);

        bagItemRef.delete()
                .addOnSuccessListener(aVoid -> {
                    bagAdapter.deleteItemFromList(projectId);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseFunction", "Error deleting bag item: ", e);
                });
    }

    public static void addToWishlist(String userId, String projectId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String wishlistDocumentId = "wishlist_" + projectId;

        Map<String, Object> projectData = new HashMap<>();
        projectData.put("projectId", projectId);

        db.collection("users").document(userId)
                .collection("wishlist")
                .document(wishlistDocumentId)
                .set(projectData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Donate", "Project added to wishlist with ID: " + wishlistDocumentId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Donate", "Error adding project to wishlist", e);
                });
    }
}


