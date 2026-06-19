package com.crowdcast.boomvilmap.repository;

import com.crowdcast.boomvilmap.model.Spot;

import java.util.ArrayList;
import java.util.List;

public class SpotRepository {

    public static List<Spot> getSpots() {
        List<Spot> spots = new ArrayList<>();

        // map 브랜치 전용: 서울 카메라 락 권역 안의 랜드마크 5개 더미 데이터셋
        spots.add(new Spot(1, "광화문·덕수궁", "서울", "역사/문화",
                "https://images.unsplash.com/photo-1599033769063-fcd3ef816810?w=800&h=400&fit=crop&auto=format",
                Spot.Level.VERY_CROWDED, 12480, "조선 왕조의 법궁인 경복궁과 근대 역사가 살아 숨 쉬는 덕수궁 권역입니다. 주말에는 문화 행사로 인파가 몰릴 수 있습니다.", 37.5759, 126.9768));

        spots.add(new Spot(2, "강남역", "서울", "상권/쇼핑",
                "https://images.unsplash.com/photo-1647767444020-01b866b7e00c?w=800&h=400&fit=crop&auto=format",
                Spot.Level.CROWDED, 28450, "대한민국 최대 규모의 지하상가와 오피스 밀집 지역입니다. 평일 출퇴근 시간대와 금요일 저녁 시간에 매우 혼잡합니다.", 37.4979, 127.0276));

        spots.add(new Spot(3, "홍대거리", "서울", "문화/예술",
                "https://images.unsplash.com/photo-1613186448181-7ba25cc0ff2a?w=800&h=400&fit=crop&auto=format",
                Spot.Level.NORMAL, 18920, "버스킹 문화와 인디 예술, 젊음의 에너지가 넘치는 거리입니다. 저녁 시간대 및 주말에 유동인구가 급격히 증가합니다.", 37.5509, 126.9244));

        spots.add(new Spot(4, "명동거리", "서울", "상권/쇼핑",
                "https://images.unsplash.com/photo-1668999980247-db74bc8be117?w=800&h=400&fit=crop&auto=format",
                Spot.Level.FREE, 9410, "먹거리 노점과 글로벌 브랜드 상점들이 밀집한 대표적인 관광 쇼핑 명소입니다. 상대적으로 평일 낮 시간대는 한산합니다.", 37.5635, 126.9846));

        spots.add(new Spot(5, "남산서울타워", "서울", "자연/전망",
                "https://images.unsplash.com/photo-1712739034224-2904f23c4c5f?w=800&h=400&fit=crop&auto=format",
                Spot.Level.NORMAL, 5640, "서울 시내를 한눈에 내려다볼 수 있는 대표적인 전망대입니다. 도심 속 자연을 느끼며 산책하기 좋으며 일몰 시간대에 방문객이 많습니다.", 37.5511, 126.9882));

        return spots;
    }

    // 관광지 검색
    public static List<Spot> searchSpots(String query) {
        List<Spot> allSpots = getSpots();
        List<Spot> filteredSpots = new ArrayList<>();

        // 키워드 필터링
        if (query == null || query.trim().isEmpty()) {
            filteredSpots.addAll(allSpots);
        } else {
            String cleanQuery = query.toLowerCase().trim();
            for (Spot spot : allSpots) {
                if (spot.name.toLowerCase().contains(cleanQuery) ||
                        spot.region.toLowerCase().contains(cleanQuery)) {
                    filteredSpots.add(spot);
                }
            }
        }

        // 정렬 알고리즘
        filteredSpots.sort((spot1, spot2) -> {
            // 혼잡도 레벨이 없을 경우 예외 방지 안전장치
            int level1 = spot1.level != null ? spot1.level.ordinal() : 0;
            int level2 = spot2.level != null ? spot2.level.ordinal() : 0;

            if (level1 != level2) {
                // 혼잡도 내림차순 정렬
                return Integer.compare(level2, level1);
            } else {
                // 혼잡도가 같으면 이름 가나다/ABC 오름차순 정렬
                return spot1.name.compareTo(spot2.name);
            }
        });

        return filteredSpots;
    }

    public static Spot findById(int id) {
        for (Spot spot : getSpots()) {
            if (spot.id == id) return spot;
        }
        return getSpots().get(0);
    }

    // 주간 차트(WeeklyCongestionChartView)에 부어줄 4단계 일주일 단위 더미 데이터 생성기
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

    // 하버사인 공식을 이용해 두 위경도 사이의 거리를 미터(m) 단위로 계산
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

        return R * c; // 거리 (m)
    }

    // 기준 위치에서 가장 가까운 상위 5개 관광지 반환
    public static List<Spot> getNearbySpots(double currentLat, double currentLng) {
        List<Spot> allSpots = new ArrayList<>(getSpots());

        // 거리 기준 오름차순 정렬
        allSpots.sort((spot1, spot2) -> {
            double dist1 = calculateDistance(currentLat, currentLng, spot1.lat, spot1.lng);
            double dist2 = calculateDistance(currentLat, currentLng, spot2.lat, spot2.lng);
            return Double.compare(dist1, dist2);
        });

        // 상위 5개만 추출 (데이터가 5개 미만일 경우를 대비해 Math.min 안전장치)
        return allSpots.subList(0, Math.min(5, allSpots.size()));
    }
}