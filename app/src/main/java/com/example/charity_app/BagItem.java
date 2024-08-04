package com.example.charity_app;

import static com.example.charity_app.FirebaseFunction.getManualImageResource;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class BagItem implements Parcelable {

    private String projectId;
    private double donationAmount;

    // No-argument constructor required for Firestore deserialization
    public BagItem() {
        // Empty constructor
    }

    public BagItem(String projectId, double donationAmount, int imageResource) {
        this.projectId = projectId;
        this.donationAmount = donationAmount;

        Log.d("BagItem", "Created BagItem with projectId: " + projectId);
    }

    // Parcelable implementation
    protected BagItem(Parcel in) {
        projectId = in.readString();
        donationAmount = in.readDouble();
    }

    public static final Creator<BagItem> CREATOR = new Creator<BagItem>() {
        @Override
        public BagItem createFromParcel(Parcel in) {
            return new BagItem(in);
        }

        @Override
        public BagItem[] newArray(int size) {
            return new BagItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(projectId);
        dest.writeDouble(donationAmount);
    }

    public void setDonationAmount(double donationAmount) {
        this.donationAmount = donationAmount;
    }

    public String getDocumentId() {
        String[] pathSegments = projectId.split("/");
        return pathSegments[pathSegments.length - 1];
    }

    public String getProjectId() {
        return projectId;
    }

    public double getDonationAmount() {
        return donationAmount;
    }

    public int getImageResource() {
        return getManualImageResource(projectId);
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}


