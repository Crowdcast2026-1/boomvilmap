package com.crowdcast.boomvilmap.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crowdcast.boomvilmap.R;
import com.crowdcast.boomvilmap.adepter.SpotAdapter;
import com.crowdcast.boomvilmap.model.Spot;
import com.crowdcast.boomvilmap.repository.SpotRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final double DEFAULT_LAT = 37.5559;
    private static final double DEFAULT_LNG = 126.9723;
    private static final String TAG = "MapFragment";

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private SpotAdapter mAdapter;
    private TextView textViewAll;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    private final Set<Spot.Level> visibleLevels = EnumSet.allOf(Spot.Level.class);
    private double currentLat = DEFAULT_LAT;
    private double currentLng = DEFAULT_LNG;
    private boolean showingAllSpots = false;
    private String lastMarkerRenderKey = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        RecyclerView recyclerView = view.findViewById(R.id.recycler_spots);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        mAdapter = new SpotAdapter(new ArrayList<>(), spotId ->
                ((MainActivity) requireActivity()).showDetail(spotId)
        );
        recyclerView.setAdapter(mAdapter);

        setupBottomSheet(view);
        setupSearch(view);
        setupFilter(view);
        setupViewAll(view);
        loadMapData();
    }

    private void setupBottomSheet(View view) {
        View bottomSheet = view.findViewById(R.id.bottom_sheet_spots);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setDraggable(true);
        bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.map_bottom_sheet_peek_height));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void setupSearch(View view) {
        ImageView imageMapSearchIcon = view.findViewById(R.id.image_map_search_icon);
        EditText editMapSearch = view.findViewById(R.id.edit_map_search);

        imageMapSearchIcon.setOnClickListener(v -> {
            String query = editMapSearch.getText().toString().trim();
            if (!query.isEmpty() && mMap != null) {
                searchMarkerAndMoveCamera(query);
            }
            hideKeyboard(editMapSearch);
        });

        editMapSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String query = editMapSearch.getText().toString().trim();
                if (!query.isEmpty() && mMap != null) {
                    searchMarkerAndMoveCamera(query);
                }
                hideKeyboard(editMapSearch);
                return true;
            }
            return false;
        });
    }

    private void setupFilter(View view) {
        ImageButton buttonFilter = view.findViewById(R.id.button_filter);
        buttonFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void setupViewAll(View view) {
        textViewAll = view.findViewById(R.id.text_view_all);
        textViewAll.setOnClickListener(v -> {
            showingAllSpots = !showingAllSpots;
            if (showingAllSpots && bottomSheetBehavior != null) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            updateSpotList();
        });
        updateViewAllLabel();
    }

    private void loadMapData() {
        SpotRepository.fetchRealTimeSpots(new SpotRepository.OnSpotsLoadedListener() {
            @Override
            public void onSuccess(List<Spot> spots) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    requestLocationUpdate();
                    refreshMapMarkers();
                    updateSpotList();
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "데이터 로드 실패: " + message, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showFilterDialog() {
        String[] labels = {"여유", "보통", "약간 붐빔", "붐빔"};
        Spot.Level[] levels = {
                Spot.Level.FREE,
                Spot.Level.NORMAL,
                Spot.Level.CROWDED,
                Spot.Level.VERY_CROWDED
        };
        boolean[] checked = new boolean[levels.length];
        for (int i = 0; i < levels.length; i++) {
            checked[i] = visibleLevels.contains(levels[i]);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("혼잡도 필터")
                .setMultiChoiceItems(labels, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("적용", (dialog, which) -> {
                    visibleLevels.clear();
                    for (int i = 0; i < levels.length; i++) {
                        if (checked[i]) visibleLevels.add(levels[i]);
                    }
                    refreshMapAndList();
                })
                .setNeutralButton("전체", (dialog, which) -> {
                    visibleLevels.clear();
                    visibleLevels.addAll(EnumSet.allOf(Spot.Level.class));
                    refreshMapAndList();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void searchMarkerAndMoveCamera(String query) {
        long startedAtMs = System.currentTimeMillis();
        String cleanQuery = query.toLowerCase(Locale.KOREA);
        Spot targetSpot = null;
        for (Spot spot : getFilteredSpots()) {
            if (spot.name != null && spot.name.toLowerCase(Locale.KOREA).contains(cleanQuery)) {
                targetSpot = spot;
                break;
            }
        }

        if (targetSpot != null) {
            LatLng targetLatLng = new LatLng(targetSpot.lat, targetSpot.lng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 16f));
            android.util.Log.d(TAG, "search completed in " + (System.currentTimeMillis() - startedAtMs) + "ms");
        } else {
            Toast.makeText(requireContext(), "일치하는 관광지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            android.util.Log.d(TAG, "search miss in " + (System.currentTimeMillis() - startedAtMs) + "ms");
        }
    }

    private void requestLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            currentLat = DEFAULT_LAT;
            currentLng = DEFAULT_LNG;
            updateSpotList();
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
            } else {
                currentLat = DEFAULT_LAT;
                currentLng = DEFAULT_LNG;
            }
            updateSpotList();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return;

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdate();
        } else {
            currentLat = DEFAULT_LAT;
            currentLng = DEFAULT_LNG;
            updateSpotList();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        LatLng southwest = new LatLng(37.4132, 126.7641);
        LatLng northeast = new LatLng(37.6824, 127.1843);
        LatLngBounds seoulBounds = new LatLngBounds(southwest, northeast);
        mMap.setLatLngBoundsForCameraTarget(seoulBounds);

        LatLng seoulStation = new LatLng(DEFAULT_LAT, DEFAULT_LNG);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoulStation, 13));
        mMap.setMinZoomPreference(10.0f);

        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() != null) {
                int spotId = (int) marker.getTag();
                ((MainActivity) requireActivity()).showDetail(spotId);
                return true;
            }
            return false;
        });

        refreshMapMarkers();
    }

    private void refreshMapAndList() {
        refreshMapMarkers();
        updateSpotList();
    }

    private void refreshMapMarkers() {
        if (mMap == null) return;

        long startedAtMs = System.currentTimeMillis();
        List<Spot> filteredSpots = getFilteredSpots();
        String markerRenderKey = buildMarkerRenderKey(filteredSpots);
        if (markerRenderKey.equals(lastMarkerRenderKey)) {
            android.util.Log.d(TAG, "skip marker redraw: unchanged data");
            return;
        }

        mMap.clear();
        for (Spot spot : filteredSpots) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(spot.lat, spot.lng))
                    .title(spot.name)
                    .snippet("실시간 혼잡도: " + levelToLabel(spot.level))
                    .icon(BitmapDescriptorFactory.defaultMarker(levelToMarkerColor(spot.level))));

            if (marker != null) {
                marker.setTag(spot.id);
            }
        }
        lastMarkerRenderKey = markerRenderKey;
        android.util.Log.d(TAG, "markers redrawn: " + filteredSpots.size() + " spots in " + (System.currentTimeMillis() - startedAtMs) + "ms");
    }

    private void updateSpotList() {
        if (mAdapter == null) return;

        long startedAtMs = System.currentTimeMillis();
        List<Spot> filteredSpots = getFilteredSpots();
        List<Spot> displaySpots;
        if (showingAllSpots) {
            displaySpots = filteredSpots;
        } else {
            displaySpots = SpotRepository.getNearbySpots(currentLat, currentLng, filteredSpots);
        }

        mAdapter.updateData(displaySpots);
        updateViewAllLabel();
        android.util.Log.d(TAG, "nearby/list updated: " + displaySpots.size() + " spots in " + (System.currentTimeMillis() - startedAtMs) + "ms");
    }

    private List<Spot> getFilteredSpots() {
        List<Spot> filteredSpots = new ArrayList<>();
        for (Spot spot : SpotRepository.getSpots()) {
            Spot.Level level = spot.level != null ? spot.level : Spot.Level.FREE;
            if (visibleLevels.contains(level)) {
                filteredSpots.add(spot);
            }
        }
        return filteredSpots;
    }

    private String buildMarkerRenderKey(List<Spot> spots) {
        StringBuilder builder = new StringBuilder(spots.size() * 16);
        for (Spot spot : spots) {
            builder.append(spot.id)
                    .append(':')
                    .append(spot.level)
                    .append(';');
        }
        return builder.toString();
    }

    private void updateViewAllLabel() {
        if (textViewAll == null) return;
        textViewAll.setText(showingAllSpots ? "주변보기" : getString(R.string.view_all));
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private String levelToLabel(Spot.Level level) {
        if (level == null) return "여유";
        switch (level) {
            case VERY_CROWDED: return "붐빔";
            case CROWDED: return "약간 붐빔";
            case NORMAL: return "보통";
            case FREE:
            default: return "여유";
        }
    }

    private float levelToMarkerColor(Spot.Level level) {
        if (level == null) return BitmapDescriptorFactory.HUE_GREEN;
        switch (level) {
            case VERY_CROWDED: return BitmapDescriptorFactory.HUE_RED;
            case CROWDED: return BitmapDescriptorFactory.HUE_ORANGE;
            case NORMAL: return BitmapDescriptorFactory.HUE_YELLOW;
            case FREE:
            default: return BitmapDescriptorFactory.HUE_GREEN;
        }
    }
}
