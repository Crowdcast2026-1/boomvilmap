package com.crowdcast.boomvilmap.repository;

import com.crowdcast.boomvilmap.model.Spot;

import java.util.ArrayList;
import java.util.List;

public class SpotRepository {

    public static List<Spot> getSpots() {
        List<Spot> spots = new ArrayList<>();

        // 🗺️ map 브랜치 전용: 서울 카메라 락 권역 안의 랜드마크 5개 더미 데이터셋
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

    public static Spot findById(int id) {
        for (Spot spot : getSpots()) {
            if (spot.id == id) return spot;
        }
        return getSpots().get(0);
    }

    // 📊 주간 차트(WeeklyCongestionChartView)에 부어줄 영문 4단계 일주일 단위 더미 데이터 생성기
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
}