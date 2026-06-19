package com.crowdcast.boomvilmap.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crowdcast.boomvilmap.R;
import com.crowdcast.boomvilmap.adepter.SpotAdapter;
import com.crowdcast.boomvilmap.model.Spot;
import com.crowdcast.boomvilmap.repository.SpotRepository;
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

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 구글 맵 비동기 로딩 시작
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.google_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 하단 BottomSheet 리사이클러뷰 바인딩
        RecyclerView recyclerView = view.findViewById(R.id.recycler_spots);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new SpotAdapter(SpotRepository.getSpots(), spotId ->
                ((MainActivity) requireActivity()).showDetail(spotId)
        ));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        MarkerManager markerManager = new com.google.maps.android.collections.MarkerManager(mMap);
        Collection markerCollection = markerManager.newCollection();

        // 서울 지역 한정 카메라 이동 범위 락(Lock) 세팅 (유지)
        LatLng southwest = new LatLng(37.4132, 126.7641);
        LatLng northeast = new LatLng(37.6824, 127.1843);
        LatLngBounds seoulBounds = new LatLngBounds(southwest, northeast);
        mMap.setLatLngBoundsForCameraTarget(seoulBounds);

        // 초기 카메라 핀포인트를 [서울역]으로 설정하고 줌 레벨을 15로 줌인
        LatLng seoulStation = new LatLng(37.5559, 126.9723);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoulStation, 15));

        // 너무 멀리서 축소해서 전국 지도가 보이는 것을 방지 (최소 줌 제한)
        mMap.setMinZoomPreference(10.0f);

        // 내부 데이터 연동 마커 드롭
        drawMarkersToCollection(markerCollection);

        // 마커 클릭 시 상세 페이지(DetailFragment) 라우팅 이벤트 처리
        markerCollection.setOnMarkerClickListener(marker -> {
            if (marker.getTag() != null) {
                int spotId = (int) marker.getTag();
                ((MainActivity) requireActivity()).showDetail(spotId);
                return true;
            }
            return false;
        });
    }

    private void drawMarkersToCollection(com.google.maps.android.collections.MarkerManager.Collection collection) {
        List<Spot> spots = SpotRepository.getSpots();
        if (spots == null || spots.isEmpty()) return;

        for (Spot spot : spots) {
            LatLng position = new LatLng(spot.lat, spot.lng);

            float color = BitmapDescriptorFactory.HUE_AZURE;
            String snippetLabel = "보통";

            if (spot.level != null) {
                switch (spot.level) {
                    case FREE:
                        color = BitmapDescriptorFactory.HUE_GREEN;
                        snippetLabel = "여유";
                        break;
                    case NORMAL:
                        color = BitmapDescriptorFactory.HUE_YELLOW;
                        snippetLabel = "보통";
                        break;
                    case CROWDED:
                        color = BitmapDescriptorFactory.HUE_ORANGE;
                        snippetLabel = "약간 붐빔";
                        break;
                    case VERY_CROWDED:
                        color = BitmapDescriptorFactory.HUE_RED;
                        snippetLabel = "붐빔";
                        break;
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