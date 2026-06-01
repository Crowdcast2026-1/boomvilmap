package com.crowdcast.boomvilmap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.SpotViewHolder> {
    public interface OnSpotClickListener {
        void onSpotClick(int spotId);
    }

    private final List<Spot> spots;
    private final OnSpotClickListener listener;

    public SpotAdapter(List<Spot> spots, OnSpotClickListener listener) {
        this.spots = spots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spot, parent, false);
        return new SpotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpotViewHolder holder, int position) {
        Spot spot = spots.get(position);

        holder.name.setText(spot.name);
        holder.region.setText(spot.region);
        holder.category.setText(spot.category);
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
    }

    @Override
    public int getItemCount() {
        return spots == null ? 0 : spots.size();
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

    static class SpotViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView region;
        TextView category;
        TextView level;

        SpotViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_spot);
            name = itemView.findViewById(R.id.text_spot_name);
            region = itemView.findViewById(R.id.text_region);
            category = itemView.findViewById(R.id.text_category);
            level = itemView.findViewById(R.id.text_level);
        }
    }
}
