package com.example.charity_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.example.charity_app.Payment;

public class DonationOptionsDialogFragment extends DialogFragment {

    private String projectId;

    public static DonationOptionsDialogFragment newInstance(String projectId) {
        DonationOptionsDialogFragment fragment = new DonationOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putString("projectId", projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            projectId = getArguments().getString("projectId");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Donation Options")
                .setMessage("Would you like to add to the bag or proceed to payment?")
                .setPositiveButton("Add to Bag", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle "Add to Bag" action
                        addToBag();
                    }
                })
                .setNegativeButton("Proceed to Payment", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Pass projectId to Payment activity
                        Intent paymentIntent = new Intent(getActivity(), Payment.class);
                        paymentIntent.putExtra("projectId", projectId);
                        startActivity(paymentIntent);
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle "Cancel" action if needed
                    }
                });

        return builder.create();
    }

    private void addToBag() {
        // Implement your addToBag logic here
    }
}
