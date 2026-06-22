package com.crowdcast.boomvilmap.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crowdcast.boomvilmap.R;
import com.crowdcast.boomvilmap.adepter.FavoriteAdapter;
import com.crowdcast.boomvilmap.model.Spot;
import com.crowdcast.boomvilmap.repository.FavoriteRepository;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {
    private FavoriteRepository favoriteRepository;
    private FavoriteAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout emptyLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        favoriteRepository = new FavoriteRepository();
        recyclerView = view.findViewById(R.id.recycler_favorites);
        emptyLayout = view.findViewById(R.id.layout_empty_favorites);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FavoriteAdapter(
                new ArrayList<>(),
                spotId -> ((MainActivity) requireActivity()).showDetail(spotId),
                this::deleteFavorite
        );
        recyclerView.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
        favoriteRepository.loadFavorites(new FavoriteRepository.FavoritesCallback() {
            @Override
            public void onSuccess(List<Spot> favorites) {
                if (!isAdded()) return;
                adapter.updateData(favorites);
                updateEmptyState(favorites.isEmpty());
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                updateEmptyState(true);
                Toast.makeText(requireContext(), "즐겨찾기 로드 실패: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFavorite(int spotId, int adapterPosition) {
        favoriteRepository.removeFavorite(spotId, new FavoriteRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                adapter.removeAt(adapterPosition);
                updateEmptyState(adapter.getItemCount() == 0);
                Toast.makeText(requireContext(), "즐겨찾기에서 제거했습니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "즐겨찾기 삭제 실패: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
