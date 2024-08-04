package com.example.charity_app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.example.charity_app.BagItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class BagAdapter extends BaseAdapter {

    private Context context;
    private List<BagItem> bagItems;
    private String userId;

    public BagAdapter(Context context, List<BagItem> bagItems, String userId) {
        this.context = context;
        this.bagItems = bagItems;
        this.userId = userId;
    }

    @Override
    public int getCount() {
        return bagItems.size();
    }

    @Override
    public Object getItem(int position) {
        return bagItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.bag_item, null);
        }

        // Get views from layout
        ImageView projectImage = convertView.findViewById(R.id.projectImage);
        TextView projectName = convertView.findViewById(R.id.projectName);
        TextView donationAmount = convertView.findViewById(R.id.donationAmount);
        TextView editButton = convertView.findViewById(R.id.editButton);
        ImageView deleteButton = convertView.findViewById(R.id.deleteButton);
        Button payButton = convertView.findViewById(R.id.payButton);

        // Set data to views
        BagItem bagItem = bagItems.get(position);
        int imageResourceId = bagItem.getImageResource();
        projectImage.setImageResource(imageResourceId);
        projectName.setText(bagItem.getDocumentId());
        donationAmount.setText("Donation Amount: $" + bagItem.getDonationAmount());

        // Handle edit and delete actions
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(bagItem);
            }
        });

        // Set click listener for deleteButton
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(bagItem);
            }
        });

        // Set click listener for payButton
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Payment activity and pass project details
                startPaymentActivity(bagItem);
            }
        });

        return convertView;
    }

    private void showEditDialog(BagItem bagItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Donation Amount");
        builder.setMessage("Enter the new donation amount:");

        EditText inputAmount = new EditText(context);
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(inputAmount);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newAmount = inputAmount.getText().toString();
                try {
                    double amount = Double.parseDouble(newAmount);
                    // Update the donation amount in the bagItem and notify the adapter
                    bagItem.setDonationAmount(amount);
                    notifyDataSetChanged();
                } catch (NumberFormatException e) {
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Custom method to update data
    public void updateData(List<BagItem> newBagItems) {
        bagItems.clear();
        bagItems.addAll(newBagItems);
        notifyDataSetChanged();
    }

    // Helper method to get image resource ID
    private int getImageResourceId(Context context, String imageName) {
        return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
    }

    private void startPaymentActivity(BagItem bagItem) {
        Log.d("BagAdapter", "Starting Payment Activity with projectName: " + bagItem.getDocumentId());
        Intent paymentIntent = new Intent(context, Payment.class);
        paymentIntent.putExtra("projectName", bagItem.getDocumentId());
        paymentIntent.putExtra("donationAmount", bagItem.getDonationAmount());
        context.startActivity(paymentIntent);
    }

    // show a confirmation dialog for deletion
    private void showDeleteConfirmationDialog(BagItem bagItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this item from your bag?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle item deletion
                deleteItemFromBag(bagItem);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Add this method to handle item deletion
    public void deleteItemFromBag(BagItem bagItem) {
        // Get userId from your authentication system (replace "dummyUserId" with actual user ID)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            FirebaseFunction.removeFromBag(userId, bagItem.getProjectId(), this);
        } else {
         //
        }
    }

    // Add this method to get the DocumentReference for a bag item in Firestore
    private DocumentReference getFirestoreDocumentReference(String projectId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("userBags").document(userId)
                .collection("bagItems").document(projectId);
    }

    public void deleteItemFromList(String projectId) {
        // Find the BagItem with the given projectId and remove it from the list
        for (BagItem bagItem : bagItems) {
            if (bagItem.getProjectId().equals(projectId)) {
                bagItems.remove(bagItem);
                notifyDataSetChanged();
                break;  // Exit the loop after the first matching item is removed
            }
        }
    }
}


