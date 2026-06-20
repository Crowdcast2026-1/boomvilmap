package com.crowdcast.boomvilmap.repository;

import com.crowdcast.boomvilmap.model.CollectAllResponse;
import com.crowdcast.boomvilmap.model.CurrentPopulationResponse;
import com.crowdcast.boomvilmap.model.PredictionResponse;
import com.crowdcast.boomvilmap.model.Spot;
import com.crowdcast.boomvilmap.network.RetrofitClient;
import com.crowdcast.boomvilmap.network.SeoulCrowdApiService;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpotRepository {
    private static final int API_RETRY_COUNT = 1;
    private static final long POPULATION_CACHE_MAX_AGE_MS = 60_000L;
    private static final String TAG = "SpotRepository";
    private static List<Spot> cachedSpots = new ArrayList<>();
    private static long lastPopulationFetchAtMs = 0L;
    private static boolean populationRequestInFlight = false;

    public interface OnSpotsLoadedListener {
        void onSuccess(List<Spot> spots);
        void onError(String message);
    }

    public interface OnSpotLoadedListener {
        void onSuccess(Spot spot);
        void onError(String message);
    }

    public interface OnWeeklyPredictionLoadedListener {
        void onSuccess(List<Spot.Level> weeklyLevels, List<String> labels);
        void onError(String message);
    }

    public static void fetchRealTimeSpots(OnSpotsLoadedListener listener) {
        long startedAtMs = System.currentTimeMillis();
        long now = System.currentTimeMillis();

        if (!cachedSpots.isEmpty()) {
            android.util.Log.d(TAG, "emit cached spots in " + (System.currentTimeMillis() - startedAtMs) + "ms");
            listener.onSuccess(new ArrayList<>(cachedSpots));

            if (now - lastPopulationFetchAtMs < POPULATION_CACHE_MAX_AGE_MS) {
                android.util.Log.d(TAG, "skip refresh: population cache is still fresh");
                return;
            }
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("spots").get().addOnCompleteListener(task -> {
            android.util.Log.d(TAG, "Firestore spots loaded in " + (System.currentTimeMillis() - startedAtMs) + "ms");
            if (!task.isSuccessful() || task.getResult() == null) {
                listener.onError("Firebase 데이터를 불러오지 못했습니다.");
                return;
            }

            QuerySnapshot snapshot = task.getResult();
            if (snapshot.isEmpty()) {
                cachedSpots = new ArrayList<>();
                listener.onSuccess(cachedSpots);
                return;
            }

            if (cachedSpots.isEmpty()) {
                List<Spot> baseSpots = buildSpots(snapshot, new HashMap<>());
                cachedSpots = baseSpots;
                android.util.Log.d(TAG, "emit base spots before population API in " + (System.currentTimeMillis() - startedAtMs) + "ms");
                listener.onSuccess(new ArrayList<>(baseSpots));
            }

            if (populationRequestInFlight) {
                android.util.Log.d(TAG, "skip duplicate population API request");
                return;
            }

            populationRequestInFlight = true;
            SeoulCrowdApiService api = RetrofitClient.getApiService();
            fetchAllPopulation(api, snapshot, listener, API_RETRY_COUNT, startedAtMs);
        });
    }

    public static void fetchCurrentSpot(Spot baseSpot, OnSpotLoadedListener listener) {
        if (baseSpot == null || baseSpot.name == null) {
            listener.onError("관광지 정보를 찾을 수 없습니다.");
            return;
        }

        SeoulCrowdApiService api = RetrofitClient.getApiService();
        api.getCurrentPopulation(baseSpot.name).enqueue(new Callback<CurrentPopulationResponse>() {
            @Override
            public void onResponse(Call<CurrentPopulationResponse> call, Response<CurrentPopulationResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    listener.onError("상세 혼잡도 API 응답이 올바르지 않습니다.");
                    return;
                }

                Spot updatedSpot = applyPopulation(baseSpot, response.body());
                updateCachedSpot(updatedSpot);
                listener.onSuccess(updatedSpot);
            }

            @Override
            public void onFailure(Call<CurrentPopulationResponse> call, Throwable t) {
                listener.onError("상세 혼잡도 API 호출 실패: " + t.getMessage());
            }
        });
    }

    public static void fetchWeeklyPrediction(Spot spot, OnWeeklyPredictionLoadedListener listener) {
        if (spot == null || spot.name == null) {
            listener.onError("관광지 정보를 찾을 수 없습니다.");
            return;
        }

        int days = 7;
        Spot.Level[] levels = new Spot.Level[days];
        String[] labels = new String[days];
        AtomicInteger pending = new AtomicInteger(days);
        AtomicBoolean hasSuccess = new AtomicBoolean(false);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA);
        String targetTime = new SimpleDateFormat("HH:00", Locale.KOREA).format(calendar.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        SimpleDateFormat labelFormat = new SimpleDateFormat("E", Locale.KOREA);

        SeoulCrowdApiService api = RetrofitClient.getApiService();
        for (int i = 0; i < days; i++) {
            Calendar target = (Calendar) calendar.clone();
            target.add(Calendar.DAY_OF_YEAR, i);
            String targetDate = dateFormat.format(target.getTime());
            labels[i] = labelFormat.format(target.getTime());

            final int index = i;
            api.getPrediction(spot.name, targetDate, targetTime).enqueue(new Callback<PredictionResponse>() {
                @Override
                public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        levels[index] = Spot.parseLevel(response.body().predicted_congestion_level);
                        hasSuccess.set(true);
                    }
                    finishPredictionCall();
                }

                @Override
                public void onFailure(Call<PredictionResponse> call, Throwable t) {
                    finishPredictionCall();
                }

                private void finishPredictionCall() {
                    if (pending.decrementAndGet() != 0) return;

                    if (!hasSuccess.get()) {
                        listener.onError("주간 예측 API 호출에 실패했습니다.");
                        return;
                    }

                    List<Spot.Level> resultLevels = new ArrayList<>();
                    List<String> resultLabels = new ArrayList<>();
                    for (int j = 0; j < days; j++) {
                        if (levels[j] == null) continue;
                        resultLevels.add(levels[j]);
                        resultLabels.add(labels[j]);
                    }
                    listener.onSuccess(resultLevels, resultLabels);
                }
            });
        }
    }

    public static List<Spot> getSpots() {
        return cachedSpots;
    }

    public static List<Spot> searchSpots(String query) {
        List<Spot> allSpots = getSpots();
        List<Spot> filteredSpots = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            filteredSpots.addAll(allSpots);
        } else {
            String cleanQuery = query.toLowerCase().trim();
            for (Spot spot : allSpots) {
                boolean nameMatches = spot.name != null && spot.name.toLowerCase().contains(cleanQuery);
                boolean regionMatches = spot.region != null && spot.region.toLowerCase().contains(cleanQuery);
                if (nameMatches || regionMatches) {
                    filteredSpots.add(spot);
                }
            }
        }

        filteredSpots.sort((spot1, spot2) -> {
            int level1 = spot1.level != null ? spot1.level.ordinal() : 0;
            int level2 = spot2.level != null ? spot2.level.ordinal() : 0;

            if (level1 != level2) {
                return Integer.compare(level2, level1);
            }
            return (spot1.name != null && spot2.name != null) ? spot1.name.compareTo(spot2.name) : 0;
        });

        return filteredSpots;
    }

    public static Spot findById(int id) {
        Spot spot = findByIdOrNull(id);
        if (spot != null) return spot;
        return getSpots().isEmpty() ? null : getSpots().get(0);
    }

    public static Spot findByIdOrNull(int id) {
        for (Spot spot : getSpots()) {
            if (spot.id == id) return spot;
        }
        return null;
    }

    public static List<Spot.Level> getWeeklyPrediction(int spotId) {
        return new ArrayList<>();
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double radius = 6371e3;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return radius * c;
    }

    public static List<Spot> getNearbySpots(double currentLat, double currentLng) {
        return getNearbySpots(currentLat, currentLng, getSpots());
    }

    public static List<Spot> getNearbySpots(double currentLat, double currentLng, List<Spot> sourceSpots) {
        List<Spot> allSpots = new ArrayList<>(sourceSpots);
        if (allSpots.isEmpty()) return new ArrayList<>();

        allSpots.sort((spot1, spot2) -> {
            double dist1 = calculateDistance(currentLat, currentLng, spot1.lat, spot1.lng);
            double dist2 = calculateDistance(currentLat, currentLng, spot2.lat, spot2.lng);
            return Double.compare(dist1, dist2);
        });

        return new ArrayList<>(allSpots.subList(0, Math.min(5, allSpots.size())));
    }

    private static void fetchAllPopulation(
            SeoulCrowdApiService api,
            QuerySnapshot snapshot,
            OnSpotsLoadedListener listener,
            int retriesRemaining,
            long startedAtMs
    ) {
        api.getAllPopulation().enqueue(new Callback<CollectAllResponse>() {
            @Override
            public void onResponse(Call<CollectAllResponse> call, Response<CollectAllResponse> response) {
                populationRequestInFlight = false;
                Map<String, CurrentPopulationResponse> apiDataMap = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().collected != null) {
                    for (CurrentPopulationResponse pop : response.body().collected) {
                        if (pop.area_name != null) {
                            apiDataMap.put(pop.area_name, pop);
                        }
                    }
                }

                List<Spot> spots = buildSpots(snapshot, apiDataMap);
                cachedSpots = spots;
                lastPopulationFetchAtMs = System.currentTimeMillis();
                android.util.Log.d(TAG, "population API merged in " + (lastPopulationFetchAtMs - startedAtMs) + "ms");
                listener.onSuccess(new ArrayList<>(spots));
            }

            @Override
            public void onFailure(Call<CollectAllResponse> call, Throwable t) {
                android.util.Log.e("API_ERROR_LOG", "전체 혼잡도 API 호출 실패: ", t);
                if (retriesRemaining > 0) {
                    fetchAllPopulation(api, snapshot, listener, retriesRemaining - 1, startedAtMs);
                    return;
                }

                populationRequestInFlight = false;
                if (!cachedSpots.isEmpty()) {
                    listener.onSuccess(new ArrayList<>(cachedSpots));
                    return;
                }

                List<Spot> fallbackSpots = buildSpots(snapshot, new HashMap<>());
                cachedSpots = fallbackSpots;
                listener.onSuccess(fallbackSpots);
            }
        });
    }

    private static List<Spot> buildSpots(QuerySnapshot snapshot, Map<String, CurrentPopulationResponse> apiDataMap) {
        List<Spot> spots = new ArrayList<>();
        for (QueryDocumentSnapshot document : snapshot) {
            String name = document.getString("area_name");
            String category = valueOrDefault(document.getString("category"), "");

            Double latObj = document.getDouble("lat");
            Double lngObj = document.getDouble("lng");
            double lat = latObj != null ? latObj : 0.0;
            double lng = lngObj != null ? lngObj : 0.0;

            int id = name != null ? Math.abs(name.hashCode()) : 0;
            String region = valueOrDefault(document.getString("region"), "서울");
            String imageUrl = getImageUrl(document, name, category);
            String description = valueOrDefault(document.getString("description"), "");

            Spot spot = new Spot(id, name, region, category, imageUrl, Spot.Level.FREE, 0, description, lat, lng);
            CurrentPopulationResponse population = name != null ? apiDataMap.get(name) : null;
            spots.add(population != null ? applyPopulation(spot, population) : spot);
        }
        return spots;
    }

    private static Spot applyPopulation(Spot spot, CurrentPopulationResponse population) {
        Spot.Level level = Spot.parseLevel(population.congestion_level);
        int visitors = (int) population.population_midpoint;
        String description = valueOrDefault(population.congestion_message, spot.description);
        return new Spot(
                spot.id,
                spot.name,
                spot.region,
                spot.category,
                spot.imageUrl,
                level,
                visitors,
                description,
                spot.lat,
                spot.lng
        );
    }

    private static void updateCachedSpot(Spot updatedSpot) {
        List<Spot> updatedSpots = new ArrayList<>(cachedSpots);
        for (int i = 0; i < updatedSpots.size(); i++) {
            if (updatedSpots.get(i).id == updatedSpot.id) {
                updatedSpots.set(i, updatedSpot);
                cachedSpots = updatedSpots;
                return;
            }
        }
        updatedSpots.add(updatedSpot);
        cachedSpots = updatedSpots;
    }

    public static String resolveImageUrl(String name, String category, String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            return imageUrl.trim();
        }
        return buildFallbackImageUrl(name, category);
    }

    private static String getImageUrl(QueryDocumentSnapshot document, String name, String category) {
        String imageUrl = document.getString("imageUrl");
        if (imageUrl == null || imageUrl.isEmpty()) imageUrl = document.getString("image_url");
        if (imageUrl == null || imageUrl.isEmpty()) imageUrl = document.getString("image");
        if (imageUrl == null || imageUrl.isEmpty()) imageUrl = document.getString("photo_url");
        return resolveImageUrl(name, category, imageUrl);
    }

    private static String buildFallbackImageUrl(String name, String category) {
        String query = imageQueryFor(name, category);
        String seedKey = valueOrDefault(name, valueOrDefault(category, "seoul"));
        long seed = Math.abs((long) seedKey.hashCode()) % 10000L + 1L;
        return String.format(Locale.US, "https://loremflickr.com/800/500/%s?lock=%d", query, seed);
    }

    private static String imageQueryFor(String name, String category) {
        String text = (valueOrDefault(name, "") + " " + valueOrDefault(category, "")).toLowerCase(Locale.KOREA);

        if (text.contains("공원") || text.contains("숲") || text.contains("여가") || text.contains("한강")) {
            return "seoul,park";
        }
        if (text.contains("시장") || text.contains("상권") || text.contains("거리") || text.contains("로데오")) {
            return "seoul,street";
        }
        if (text.contains("역") || text.contains("터미널") || text.contains("교통")) {
            return "seoul,city";
        }
        if (text.contains("궁") || text.contains("유적") || text.contains("역사") || text.contains("문화")) {
            return "seoul,palace";
        }
        return "seoul,landmark";
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }
}
