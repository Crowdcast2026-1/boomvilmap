package com.crowdcast.boomvilmap.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.crowdcast.boomvilmap.R;
import com.crowdcast.boomvilmap.model.Spot;
import com.crowdcast.boomvilmap.repository.SpotRepository;

public class DetailFragment extends Fragment {
    private static final String ARG_SPOT_ID = "spot_id";

    public static DetailFragment newInstance(int spotId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SPOT_ID, spotId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        int spotId = getArguments() != null ? getArguments().getInt(ARG_SPOT_ID, 1) : 1;
        Spot spot = SpotRepository.findById(spotId);

        TextView name = view.findViewById(R.id.text_detail_name);
        TextView region = view.findViewById(R.id.text_detail_region);
        TextView category = view.findViewById(R.id.text_detail_category);
        TextView description = view.findViewById(R.id.text_description);
        TextView visitors = view.findViewById(R.id.text_visitors);

        name.setText(spot.name);
        region.setText(spot.region);
        category.setText(spot.category);
        description.setText(spot.description);
        visitors.setText(String.format("%,d명 방문", spot.visitors));

        view.findViewById(R.id.button_back).setOnClickListener(v -> ((MainActivity) requireActivity()).showMap());
    }
}
