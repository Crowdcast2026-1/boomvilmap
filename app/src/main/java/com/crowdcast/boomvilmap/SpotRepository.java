package com.crowdcast.boomvilmap;

import java.util.ArrayList;
import java.util.List;

public class SpotRepository {
    public static List<Spot> getSpots() {
        List<Spot> spots = new ArrayList<>();
        spots.add(new Spot(1, "경복궁", "서울", "역사/문화",
                "https://images.unsplash.com/photo-1599033769063-fcd3ef816810?w=800&h=400&fit=crop&auto=format",
                Spot.Level.HIGH, 12480, "조선 왕조의 법궁으로 1395년에 창건된 대표 궁궐입니다.", 37.58, 126.98));
        spots.add(new Spot(2, "해운대", "부산", "해변",
                "https://images.unsplash.com/photo-1647767444020-01b866b7e00c?w=800&h=400&fit=crop&auto=format",
                Spot.Level.MODERATE, 7240, "부산의 대표 해수욕장으로 많은 관광객이 찾는 명소입니다.", 35.16, 129.16));
        spots.add(new Spot(3, "성산일출봉", "제주", "자연",
                "https://images.unsplash.com/photo-1613186448181-7ba25cc0ff2a?w=800&h=400&fit=crop&auto=format",
                Spot.Level.LOW, 3120, "유네스코 세계자연유산으로 제주도의 대표 일출 명소입니다.", 33.46, 126.94));
        spots.add(new Spot(4, "경주 불국사", "경주", "역사/문화",
                "https://images.unsplash.com/photo-1668999980247-db74bc8be117?w=800&h=400&fit=crop&auto=format",
                Spot.Level.LOW, 2850, "신라 시대에 창건된 사찰로 유네스코 세계문화유산입니다.", 35.79, 129.33));
        spots.add(new Spot(5, "남이섬", "강원", "자연",
                "https://images.unsplash.com/photo-1712739034224-2904f23c4c5f?w=800&h=400&fit=crop&auto=format",
                Spot.Level.MODERATE, 5640, "북한강 위에 위치한 반달 모양의 섬으로 드라마 촬영지로 유명합니다.", 37.79, 127.52));
        return spots;
    }

    public static Spot findById(int id) {
        for (Spot spot : getSpots()) {
            if (spot.id == id) return spot;
        }
        return getSpots().get(0);
    }
}
