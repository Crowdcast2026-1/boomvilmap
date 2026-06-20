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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.maps.android.collections.MarkerManager;
import com.google.maps.android.collections.MarkerManager.Collection;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private SpotAdapter mAdapter;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 위치 서비스 클라이언트 초기화
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // 구글 맵 비동기 로딩
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 리사이클러뷰 초기화
        RecyclerView recyclerView = view.findViewById(R.id.recycler_spots);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new SpotAdapter(new ArrayList<>(), spotId ->
                ((MainActivity) requireActivity()).showDetail(spotId)
        );
        recyclerView.setAdapter(mAdapter);

        // UI 세팅이 끝나면, 서버에 데이터를 요청
        loadMapData();

        ImageView imageMapSearchIcon = view.findViewById(R.id.image_map_search_icon);
        EditText editMapSearch = view.findViewById(R.id.edit_map_search);

        imageMapSearchIcon.setOnClickListener(v -> {
            String query = editMapSearch.getText().toString().trim();
            if (!query.isEmpty() && mMap != null) {
                searchMarkerAndMoveCamera(query);
            }

            android.view.inputmethod.InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(editMapSearch.getWindowToken(), 0);
        });

        editMapSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String query = editMapSearch.getText().toString().trim();

                if (!query.isEmpty() && mMap != null) {
                    searchMarkerAndMoveCamera(query);
                }

                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(editMapSearch.getWindowToken(), 0);

                return true;
            }
            return false;
        });
    }

    // 서버에서 데이터를 비동기로 불러오고 화면을 갱신하는 메서드
    private void loadMapData() {
        SpotRepository.fetchRealTimeSpots(new SpotRepository.OnSpotsLoadedListener() {
            @Override
            public void onSuccess(List<Spot> spots) {
                // UI 업데이트는 반드시 메인 스레드에서 실행
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // 1. 데이터가 준비되었으니 내 위치를 잡아 어댑터(리스트)를 갱신합니다.
                        requestLocationUpdate();

                        // 2. 지도 마커 다시 그리기
                        if (mMap != null) {
                            mMap.clear(); // 기존 마커 초기화
                            MarkerManager markerManager = new MarkerManager(mMap);
                            Collection markerCollection = markerManager.newCollection();

                            drawMarkersToCollection(markerCollection);

                            // 클릭 이벤트 다시 연결
                            markerCollection.setOnMarkerClickListener(marker -> {
                                if (marker.getTag() != null) {
                                    int spotId = (int) marker.getTag();
                                    ((MainActivity) requireActivity()).showDetail(spotId);
                                    return true;
                                }
                                return false;
                            });
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "데이터 로드 실패: " + message, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void searchMarkerAndMoveCamera(String query) {
        List<Spot> spots = SpotRepository.getSpots();
        Spot targetSpot = null;

        // 이름이 완벽히 일치하거나 글자가 포함되어 있는지 검색
        for (Spot spot : spots) {
            if (spot.name.toLowerCase().contains(query.toLowerCase())) {
                targetSpot = spot;
                break;
            }
        }

        if (targetSpot != null) {
            // 해당 관광지 좌표로 카메라 이동 및 줌인
            LatLng targetLatLng = new LatLng(targetSpot.lat, targetSpot.lng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 16f));
        } else {
            Toast.makeText(requireContext(), "일치하는 관광지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationUpdate() {
        // 권한 체크
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // 에뮬레이터에 설정된 최종 GPS 위치 가져오기
        mFusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                // 가져온 위경도 변수에 매칭
                double currentLat = location.getLatitude();
                double currentLng = location.getLongitude();

                // 변수를 기반으로 상위 5개 계산 후 어댑터 갱신
                List<Spot> nearbySpots = SpotRepository.getNearbySpots(currentLat, currentLng);
                mAdapter.updateData(nearbySpots);
            } else {
                // 가끔 초기 에뮬레이터 위치가 null인 경우 안전장치로 서울역 배치
                List<Spot> defaultSpots = SpotRepository.getNearbySpots(37.5559, 126.9723);
                mAdapter.updateData(defaultSpots);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdate(); // 권한 허용 시 다시 위치 요청
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // 초기 카메라 세팅 (서울 권역)
        LatLng southwest = new LatLng(37.4132, 126.7641);
        LatLng northeast = new LatLng(37.6824, 127.1843);
        LatLngBounds seoulBounds = new LatLngBounds(southwest, northeast);
        mMap.setLatLngBoundsForCameraTarget(seoulBounds);

        LatLng seoulStation = new LatLng(37.5559, 126.9723);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoulStation, 13));
        mMap.setMinZoomPreference(10.0f);

        // 데이터가 이미 로드되어 있을 경우를 대비한 초기 마커 그리기 (보통은 데이터가 나중에 오므로 0개가 그려짐)
        MarkerManager markerManager = new MarkerManager(mMap);
        Collection markerCollection = markerManager.newCollection();
        drawMarkersToCollection(markerCollection);

        markerCollection.setOnMarkerClickListener(marker -> {
            if (marker.getTag() != null) {
                int spotId = (int) marker.getTag();
                ((MainActivity) requireActivity()).showDetail(spotId);
                return true;
            }
            return false;
        });
    }

    private void drawMarkersToCollection(Collection collection) {
        List<Spot> spots = SpotRepository.getSpots();
        if (spots == null || spots.isEmpty()) return; // 데이터가 없으면 그냥 리턴

        for (Spot spot : spots) {
            LatLng position = new LatLng(spot.lat, spot.lng);
            float color = BitmapDescriptorFactory.HUE_AZURE;
            String snippetLabel = "보통";

            if (spot.level != null) {
                switch (spot.level) {
                    case FREE: color = BitmapDescriptorFactory.HUE_GREEN; snippetLabel = "여유"; break;
                    case NORMAL: color = BitmapDescriptorFactory.HUE_YELLOW; snippetLabel = "보통"; break;
                    case CROWDED: color = BitmapDescriptorFactory.HUE_ORANGE; snippetLabel = "약간 붐빔"; break;
                    case VERY_CROWDED: color = BitmapDescriptorFactory.HUE_RED; snippetLabel = "붐빔"; break;
                }
            }

            Marker marker = collection.addMarker(new MarkerOptions()
                    .position(position)
                    .title(spot.name)
                    .snippet("실시간 혼잡도: " + snippetLabel)
                    .icon(BitmapDescriptorFactory.defaultMarker(color)));

            if (marker != null) {
                marker.setTag(spot.id);
            }
        }
    }
}