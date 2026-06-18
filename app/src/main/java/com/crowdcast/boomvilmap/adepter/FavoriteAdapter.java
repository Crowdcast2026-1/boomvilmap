package com.crowdcast.boomvilmap.adepter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.crowdcast.boomvilmap.R;
import com.crowdcast.boomvilmap.model.Spot;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {
    public interface OnSpotClickListener {
        void onSpotClick(int spotId);
    }

    private final List<Spot> favorites;
    private final OnSpotClickListener listener;

    public FavoriteAdapter(List<Spot> favorites, OnSpotClickListener listener) {
        this.favorites = new ArrayList<>(favorites);
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Spot spot = favorites.get(position);

        holder.name.setText(spot.name);
        holder.region.setText(spot.region);
        holder.level.setText(getLevelLabel(spot.level));
        holder.level.setBackgroundResource(getLevelBackground(spot.level));
        holder.level.setTextColor(holder.itemView.getContext().getColor(getLevelTextColor(spot.level)));

        Glide.with(holder.image)
                .load(spot.imageUrl)
                .centerCrop()
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSpotClick(spot.id);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            favorites.remove(adapterPosition);
            notifyItemRemoved(adapterPosition);
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    private String getLevelLabel(Spot.Level level) {
        if (level == Spot.Level.LOW) return "여유";
        if (level == Spot.Level.MODERATE) return "보통";
        return "혼잡";
    }

    private int getLevelBackground(Spot.Level level) {
        if (level == Spot.Level.LOW) return R.drawable.bg_low;
        if (level == Spot.Level.MODERATE) return R.drawable.bg_moderate;
        return R.drawable.bg_high;
    }

    private int getLevelTextColor(Spot.Level level) {
        if (level == Spot.Level.LOW) return R.color.low_text;
        if (level == Spot.Level.MODERATE) return R.color.moderate_text;
        return R.color.high_text;
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView region;
        TextView level;
        ImageButton deleteButton;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_favorite);
            name = itemView.findViewById(R.id.text_favorite_name);
            region = itemView.findViewById(R.id.text_favorite_region);
            level = itemView.findViewById(R.id.text_favorite_level);
            deleteButton = itemView.findViewById(R.id.button_delete_favorite);
        }
    }
}
