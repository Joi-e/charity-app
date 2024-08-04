package com.example.charity_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<WishlistItem> wishlistItems;
    private Context context;
    private OnDonateClickListener onDonateClickListener;

    public WishlistAdapter(List<WishlistItem> wishlistItems, Context context, OnDonateClickListener onDonateClickListener) {
        this.wishlistItems = wishlistItems;
        this.context = context;
        this.onDonateClickListener = onDonateClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wishlist__item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WishlistItem item = wishlistItems.get(position);

        // Set data to views
        holder.projectNameTextView.setText(item.getProjectId());
        int imageResource = getManualImageResource(item.getProjectId());
        holder.projectImageView.setImageResource(imageResource);


        // Set click listeners for buttons (modify as needed)
        holder.donateButton.setOnClickListener(view -> {
            // Handle donate button click
            if (onDonateClickListener != null) {
                onDonateClickListener.onDonateClick(item);
            }
        });

        holder.deleteButton.setOnClickListener(view -> {
            // Handle delete button click
            removeFromWishlist(position);
        });
    }

    @Override
    public int getItemCount() {
        return wishlistItems.size();
    }

    private void removeFromWishlist(int position) {
        wishlistItems.remove(position);
        notifyDataSetChanged(); // Notify adapter that data set changed
    }

    public interface OnDonateClickListener {
        void onDonateClick(WishlistItem item);
    }

    private int getManualImageResource(String projectId) {
        switch (projectId) {
            case "hearts":
                return R.drawable.charitypink;
            case "toys":
                return R.drawable.toybox;
            case "litter":
                return R.drawable.litter;
            case "family":
                return R.drawable.family_health;
            case "feed":
                return R.drawable.feed;
            case "clothing":
                return R.drawable.clothing;
            default:
                return R.drawable.charitypink;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView projectImageView;
        TextView projectNameTextView;
        Button donateButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            projectImageView = itemView.findViewById(R.id.projectImage);
            projectNameTextView = itemView.findViewById(R.id.projectName);
            donateButton = itemView.findViewById(R.id.donateButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
