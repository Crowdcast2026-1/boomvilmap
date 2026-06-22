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
import com.crowdcast.boomvilmap.repository.SpotRepository;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {
    public interface OnSpotClickListener {
        void onSpotClick(int spotId);
    }

    public interface OnFavoriteDeleteListener {
        void onFavoriteDelete(int spotId, int adapterPosition);
    }

    private final List<Spot> favorites;
    private final OnSpotClickListener listener;
    private final OnFavoriteDeleteListener deleteListener;

    public FavoriteAdapter(List<Spot> favorites, OnSpotClickListener listener, OnFavoriteDeleteListener deleteListener) {
        this.favorites = new ArrayList<>(favorites);
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    public void updateData(List<Spot> newFavorites) {
        favorites.clear();
        favorites.addAll(newFavorites);
        notifyDataSetChanged();
    }

    public void removeAt(int adapterPosition) {
        if (adapterPosition < 0 || adapterPosition >= favorites.size()) return;
        favorites.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
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
        holder.category.setText(spot.category);
        holder.level.setText(getLevelLabel(spot.level));
        holder.level.setBackgroundResource(getLevelBackground(spot.level));
        holder.level.setTextColor(holder.itemView.getContext().getColor(getLevelTextColor(spot.level)));

        String imageUrl = SpotRepository.resolveImageUrl(spot.name, spot.category, spot.imageUrl);
        Glide.with(holder.image)
                .load(imageUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
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

            if (deleteListener != null) {
                deleteListener.onFavoriteDelete(spot.id, adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    // 실제 UI 출력 시에만 영문 4단계를 한글 4대 라벨 명세로 변환
    private String getLevelLabel(Spot.Level level) {
        if (level == null) return "여유";
        switch (level) {
            case VERY_CROWDED: return "붐빔";
            case CROWDED: return "약간 붐빔";
            case NORMAL: return "보통";
            case FREE:
            default: return "여유";
        }
    }

    private int getLevelBackground(Spot.Level level) {
        if (level == null) return R.drawable.bg_low;
        switch (level) {
            case VERY_CROWDED: return R.drawable.bg_high;
            case CROWDED: return R.drawable.bg_crowded;
            case NORMAL: return R.drawable.bg_moderate;
            case FREE:
            default: return R.drawable.bg_low;
        }
    }

    private int getLevelTextColor(Spot.Level level) {
        if (level == null) return R.color.low_text;
        switch (level) {
            case VERY_CROWDED: return R.color.high_text;
            case CROWDED: return R.color.crowded_text;
            case NORMAL: return R.color.moderate_text;
            case FREE:
            default: return R.color.low_text;
        }
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView region;
        TextView category;
        TextView level;
        ImageButton deleteButton;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_favorite);
            name = itemView.findViewById(R.id.text_favorite_name);
            region = itemView.findViewById(R.id.text_favorite_region);
            category = itemView.findViewById(R.id.text_favorite_category);
            level = itemView.findViewById(R.id.text_favorite_level);
            deleteButton = itemView.findViewById(R.id.button_delete_favorite);
        }
    }
}
