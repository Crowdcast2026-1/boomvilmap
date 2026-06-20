package com.crowdcast.boomvilmap.repository;

import android.widget.Toast;

import com.crowdcast.boomvilmap.model.CollectAllResponse;
import com.crowdcast.boomvilmap.model.CurrentPopulationResponse;
import com.crowdcast.boomvilmap.model.Spot;
import com.crowdcast.boomvilmap.network.RetrofitClient;
import com.crowdcast.boomvilmap.network.SeoulCrowdApiService;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpotRepository {

    // 파이어베이스와 API로 불러온 데이터를 메모리에 임시 저장(캐싱)하는 변수
    private static List<Spot> cachedSpots = new ArrayList<>();

    // 데이터 로딩 상태를 UI에 전달하기 위한 인터페이스
    public interface OnSpotsLoadedListener {
        void onSuccess(List<Spot> spots);
        void onError(String message);
    }

    // 실시간 혼잡도 및 관광지 데이터 비동기 로딩 (서버 최적화 버전)
    public static void fetchRealTimeSpots(OnSpotsLoadedListener listener) {
        List<Spot> realTimeSpots = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 파이어베이스에서 관광지 기본 정보 가져오기
        db.collection("spots").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {

                // 파이어베이스에 데이터가 없는 경우 안전장치
                if (task.getResult().isEmpty()) {
                    cachedSpots = realTimeSpots;
                    listener.onSuccess(realTimeSpots);
                    return;
                }

                // 전체 혼잡도 데이터를 가져오는 API 호출
                SeoulCrowdApiService api = RetrofitClient.getApiService();
                api.getAllPopulation().enqueue(new Callback<CollectAllResponse>() {
                    @Override
                    public void onResponse(Call<CollectAllResponse> call, Response<CollectAllResponse> response) {

                        // API로 받아온 데이터를 "이름"을 Key로 하는 Map에 담기 (매칭 속도 최적화: O(1))
                        Map<String, CurrentPopulationResponse> apiDataMap = new HashMap<>();
                        if (response.isSuccessful() && response.body() != null && response.body().collected != null) {
                            for (CurrentPopulationResponse pop : response.body().collected) {
                                apiDataMap.put(pop.area_name, pop);
                            }
                        }

                        // 파이어베이스 데이터와 API 데이터를 이름(area_name) 기준으로 결합
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Firebase 실제 필드명 추출
                            String name = document.getString("area_name");
                            String category = document.getString("category");

                            Double latObj = document.getDouble("lat");
                            Double lngObj = document.getDouble("lng");
                            double lat = latObj != null ? latObj : 0.0;
                            double lng = lngObj != null ? lngObj : 0.0;

                            // 고유 id가 없으므로 장소 이름의 해시코드를 임시 id로 사용
                            int id = name != null ? Math.abs(name.hashCode()) : 0;
                            String region = "서울";
                            String imageUrl = ""; // Glide 에러 방지용 빈 문자열

                            // 혼잡도 기본값 설정
                            Spot.Level currentLevel = Spot.Level.FREE;
                            int currentVisitors = 0;
                            String description = "";

                            // Firebase의 장소 이름이 API Map 데이터에 존재하면 해당 혼잡도로 덮어쓰기
                            if (name != null && apiDataMap.containsKey(name)) {
                                CurrentPopulationResponse popData = apiDataMap.get(name);
                                currentLevel = Spot.parseLevel(popData.congestion_level);
                                currentVisitors = (int) popData.population_midpoint;
                                description = popData.congestion_message; // 혼잡도 메시지를 상세 설명으로 활용
                            }

                            // 조립된 최종 Spot 객체를 리스트에 추가
                            Spot spot = new Spot(id, name, region, category, imageUrl, currentLevel, currentVisitors, description, lat, lng);
                            realTimeSpots.add(spot);
                        }

                        // 캐시 갱신 및 UI 업데이트 콜백 실행
                        cachedSpots = realTimeSpots;
                        listener.onSuccess(realTimeSpots);
                    }

                    @Override
                    public void onFailure(Call<CollectAllResponse> call, Throwable t) {
                        android.util.Log.e("API_ERROR_LOG", "통신 실패 원인: ", t);
                        // 통신 실패 시 앱 다운 방지 (모든 장소를 기본값 FREE로 세팅하여 표시)
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("area_name");
                            String category = document.getString("category");
                            int id = name != null ? Math.abs(name.hashCode()) : 0;

                            Double latObj = document.getDouble("lat");
                            Double lngObj = document.getDouble("lng");
                            double lat = latObj != null ? latObj : 0.0;
                            double lng = lngObj != null ? lngObj : 0.0;

                            Spot spot = new Spot(id, name, "서울", category, "", Spot.Level.FREE, 0, "", lat, lng);
                            realTimeSpots.add(spot);
                        }
                        cachedSpots = realTimeSpots;
                        listener.onSuccess(realTimeSpots);
                    }
                });
            } else {
                listener.onError("Firebase 데이터를 불러오지 못했습니다.");
            }
        });
    }

    // 다른 클래스(Search, Map)에서 캐시된 데이터를 즉시 꺼내 쓸 수 있도록 제공
    public static List<Spot> getSpots() {
        return cachedSpots;
    }

    // 관광지 검색 로직 (캐시 기반)
    public static List<Spot> searchSpots(String query) {
        List<Spot> allSpots = getSpots();
        List<Spot> filteredSpots = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            filteredSpots.addAll(allSpots);
        } else {
            String cleanQuery = query.toLowerCase().trim();
            for (Spot spot : allSpots) {
                if (spot.name != null && spot.name.toLowerCase().contains(cleanQuery) ||
                        spot.region != null && spot.region.toLowerCase().contains(cleanQuery)) {
                    filteredSpots.add(spot);
                }
            }
        }

        // 정렬 알고리즘: 혼잡도 높은 순 -> 이름 가나다 순
        filteredSpots.sort((spot1, spot2) -> {
            int level1 = spot1.level != null ? spot1.level.ordinal() : 0;
            int level2 = spot2.level != null ? spot2.level.ordinal() : 0;

            if (level1 != level2) {
                return Integer.compare(level2, level1); // 내림차순
            } else {
                return (spot1.name != null && spot2.name != null) ? spot1.name.compareTo(spot2.name) : 0;
            }
        });

        return filteredSpots;
    }

    // ID로 특정 관광지 찾기 (상세 페이지 용)
    public static Spot findById(int id) {
        for (Spot spot : getSpots()) {
            if (spot.id == id) return spot;
        }
        return getSpots().isEmpty() ? null : getSpots().get(0); // 빈 데이터일 때 터짐 방지
    }

    // 주간 차트용 더미 데이터
    public static List<Spot.Level> getWeeklyPrediction(int spotId) {
        List<Spot.Level> weeklyData = new ArrayList<>();

        if (spotId % 2 == 0) {
            weeklyData.add(Spot.Level.FREE);          // 월
            weeklyData.add(Spot.Level.NORMAL);        // 화
            weeklyData.add(Spot.Level.NORMAL);        // 수
            weeklyData.add(Spot.Level.CROWDED);       // 목
            weeklyData.add(Spot.Level.CROWDED);       // 금
            weeklyData.add(Spot.Level.VERY_CROWDED);  // 토
            weeklyData.add(Spot.Level.VERY_CROWDED);  // 일
        } else {
            weeklyData.add(Spot.Level.NORMAL);        // 월
            weeklyData.add(Spot.Level.FREE);          // 화
            weeklyData.add(Spot.Level.FREE);          // 수
            weeklyData.add(Spot.Level.NORMAL);        // 목
            weeklyData.add(Spot.Level.CROWDED);       // 금
            weeklyData.add(Spot.Level.VERY_CROWDED);  // 토
            weeklyData.add(Spot.Level.CROWDED);       // 일
        }
        return weeklyData;
    }

    // 하버사인 공식을 이용한 거리 계산
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371e3; // 지구 반지름 (미터 단위)
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // 내 위치 기반 가장 가까운 관광지 5개 반환 (초기 지도 화면 용)
    public static List<Spot> getNearbySpots(double currentLat, double currentLng) {
        List<Spot> allSpots = new ArrayList<>(getSpots());

        if (allSpots.isEmpty()) return new ArrayList<>(); // 초기 구동 시 안전장치

        // 거리 기준 오름차순 정렬
        allSpots.sort((spot1, spot2) -> {
            double dist1 = calculateDistance(currentLat, currentLng, spot1.lat, spot1.lng);
            double dist2 = calculateDistance(currentLat, currentLng, spot2.lat, spot2.lng);
            return Double.compare(dist1, dist2);
        });

        return allSpots.subList(0, Math.min(5, allSpots.size()));
    }
}