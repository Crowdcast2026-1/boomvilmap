package com.crowdcast.boomvilmap.adepter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.crowdcast.boomvilmap.R;
import com.crowdcast.boomvilmap.model.Spot;
import com.crowdcast.boomvilmap.repository.SpotRepository;
import java.util.List;

public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.SpotViewHolder> {
    public interface OnSpotClickListener {
        void onSpotClick(int spotId);
    }

    // 외부 데이터 교체를 위해 final 제약 해제
    private List<Spot> spots;
    private final OnSpotClickListener listener;

    public SpotAdapter(List<Spot> spots, OnSpotClickListener listener) {
        this.spots = spots;
        this.listener = listener;
    }

    // 새로운 검색 리스트로 어댑터 갱신
    public void updateData(List<Spot> newSpots) {
        this.spots = newSpots;
        notifyDataSetChanged();
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

        String imageUrl = SpotRepository.resolveImageUrl(spot.name, spot.category, spot.imageUrl);

        Glide.with(holder.image.getContext())
                .load(imageUrl)
                .placeholder(android.R.color.darker_gray) // 로딩 중이거나 이미지가 없을 때 띄울 기본 배경
                .error(android.R.color.darker_gray)       // 에러 발생 시 띄울 기본 배경
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
