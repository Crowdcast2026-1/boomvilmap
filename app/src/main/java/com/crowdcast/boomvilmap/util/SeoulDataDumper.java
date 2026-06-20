package com.crowdcast.boomvilmap.util;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeoulDataDumper {

    // 1회만 사용
    public static void checkAndDumpData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. 디비가 이미 채워져 있는지 1건만 조회해서 확인
        db.collection("spots").limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        android.util.Log.d("FIRESTORE_DUMP", "🚨 Firestore가 비어있습니다. 실제 유효한 113개 장소 덤프를 시작합니다.");
                        executeDump(db);
                    } else {
                        android.util.Log.d("FIRESTORE_DUMP", "🟢 이미 장소 데이터가 존재하므로 덤프를 건너뜁니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FIRESTORE_DUMP", "❌ 디비 확인 실패: " + e.getMessage());
                });
    }

    private static void executeDump(FirebaseFirestore db) {
        List<Map<String, Object>> spots = new ArrayList<>();

        // 🗺️ 관광특구 (7개)
        spots.add(createSpot("강남 MICE 관광특구", 37.5113, 127.0594, "상권/쇼핑"));
        spots.add(createSpot("동대문 관광특구", 37.5684, 127.0089, "상권/쇼핑"));
        spots.add(createSpot("명동 관광특구", 37.5618, 126.9844, "상권/쇼핑"));
        spots.add(createSpot("이태원 관광특구", 37.5345, 126.9946, "상권/쇼핑"));
        spots.add(createSpot("잠실 관광특구", 37.5112, 127.1001, "상권/쇼핑"));
        spots.add(createSpot("종로·청계 관광특구", 37.5692, 126.9850, "상권/쇼핑"));
        spots.add(createSpot("홍대 관광특구", 37.5509, 126.9244, "상권/쇼핑"));

        // 🏛️ 고궁/문화재 (5개)
        spots.add(createSpot("경복궁", 37.5796, 126.9770, "역사/문화"));
        spots.add(createSpot("광화문·덕수궁", 37.5759, 126.9768, "역사/문화"));
        spots.add(createSpot("보신각", 37.5699, 126.9836, "역사/문화"));
        spots.add(createSpot("서울 암사동 유적", 37.5624, 127.1305, "역사/문화"));
        spots.add(createSpot("창덕궁·종묘", 37.5744, 126.9926, "역사/문화"));

        // 🚇 주요 역세권 (39개)
        spots.add(createSpot("가산디지털단지역", 37.4812, 126.8826, "교통/역세권"));
        spots.add(createSpot("강남역", 37.4979, 127.0276, "교통/역세권"));
        spots.add(createSpot("건대입구역", 37.5404, 127.0692, "교통/역세권"));
        spots.add(createSpot("고덕역", 37.5550, 127.1541, "교통/역세권"));
        spots.add(createSpot("고속터미널역", 37.5049, 127.0049, "교통/역세권"));
        spots.add(createSpot("교대역", 37.4934, 127.0141, "교통/역세권"));
        spots.add(createSpot("구로디지털단지역", 37.4853, 126.8988, "교통/역세권"));
        spots.add(createSpot("구로역", 37.5030, 126.8820, "교통/역세권"));
        spots.add(createSpot("군자역", 37.5572, 127.0795, "교통/역세권"));
        spots.add(createSpot("대림역", 37.4932, 126.8949, "교통/역세권"));
        spots.add(createSpot("동대문역", 37.5712, 127.0116, "교통/역세권"));
        spots.add(createSpot("뚝섬역", 37.5472, 127.0474, "교통/역세권"));
        spots.add(createSpot("미아사거리역", 37.6133, 127.0301, "교통/역세권"));
        spots.add(createSpot("발산역", 37.5585, 126.8377, "교통/역세권"));
        spots.add(createSpot("사당역", 37.4765, 126.9816, "교통/역세권"));
        spots.add(createSpot("삼각지역", 37.5348, 126.9738, "교통/역세권"));
        spots.add(createSpot("서울대입구역", 37.4812, 126.9527, "교통/역세권"));
        spots.add(createSpot("서울식물원·마곡나루역", 37.5668, 126.8273, "교통/역세권"));
        spots.add(createSpot("서울역", 37.5547, 126.9706, "교통/역세권"));
        spots.add(createSpot("선릉역", 37.5045, 127.0490, "교통/역세권"));
        spots.add(createSpot("성신여대입구역", 37.5926, 127.0164, "교통/역세권"));
        spots.add(createSpot("수유역", 37.6380, 127.0257, "교통/역세권"));
        spots.add(createSpot("신논현역·논현역", 37.5088, 127.0242, "교통/역세권"));
        spots.add(createSpot("신도림역", 37.5089, 126.8913, "교통/역세권"));
        spots.add(createSpot("신림역", 37.4842, 126.9297, "교통/역세권"));
        spots.add(createSpot("신정네거리역", 37.5201, 126.8529, "교통/역세권"));
        spots.add(createSpot("신촌·이대역", 37.5566, 126.9371, "교통/역세권"));
        spots.add(createSpot("양재역", 37.4841, 127.0347, "교통/역세권"));
        spots.add(createSpot("역삼역", 37.5006, 127.0365, "교통/역세권"));
        spots.add(createSpot("연신내역", 37.6189, 126.9208, "교통/역세권"));
        spots.add(createSpot("오목교역·목동운동장", 37.5244, 126.8753, "교통/역세권"));
        spots.add(createSpot("왕십리역", 37.5612, 127.0382, "교통/역세권"));
        spots.add(createSpot("용산역", 37.5299, 126.9648, "교통/역세권"));
        spots.add(createSpot("이태원역", 37.5345, 126.9946, "교통/역세권"));
        spots.add(createSpot("장지역", 37.4787, 127.1262, "교통/역세권"));
        spots.add(createSpot("장한평역", 37.5614, 127.0646, "교통/역세권"));
        spots.add(createSpot("잠실새내역", 37.5116, 127.0861, "교통/역세권"));
        spots.add(createSpot("잠실역", 37.5133, 127.1001, "교통/역세권"));
        spots.add(createSpot("천호역", 37.5386, 127.1234, "교통/역세권"));
        spots.add(createSpot("총신대입구(이수)역", 37.4862, 126.9822, "교통/역세권"));
        spots.add(createSpot("충정로역", 37.5599, 126.9636, "교통/역세권"));
        spots.add(createSpot("합정역", 37.5494, 126.9144, "교통/역세권"));
        spots.add(createSpot("혜화역", 37.5822, 127.0018, "교통/역세권"));
        spots.add(createSpot("홍대입구역(2호선)", 37.5575, 126.9252, "교통/역세권"));
        spots.add(createSpot("회기역", 37.5898, 127.0578, "교통/역세권"));

        // 🛍️ 상권 / 핫플레이스 / 전통시장 (22개)
        spots.add(createSpot("가락시장", 37.4931, 127.1121, "상권/쇼핑"));
        spots.add(createSpot("가로수길", 37.5203, 127.0231, "상권/쇼핑"));
        spots.add(createSpot("광장(전통)시장", 37.5700, 127.0016, "상권/쇼핑"));
        spots.add(createSpot("김포공항", 37.5583, 126.8028, "교통/역세권"));
        spots.add(createSpot("남대문시장", 37.5592, 126.9776, "상권/쇼핑"));
        spots.add(createSpot("노량진", 37.5134, 126.9418, "상권/쇼핑"));
        spots.add(createSpot("덕수궁길·정동길", 37.5658, 126.9731, "역사/문화"));
        spots.add(createSpot("북촌한옥마을", 37.5829, 126.9835, "역사/문화"));
        spots.add(createSpot("북창동 먹자골목", 37.5623, 126.9786, "상권/쇼핑"));
        spots.add(createSpot("서촌", 37.5801, 126.9698, "상권/쇼핑"));
        spots.add(createSpot("성수카페거리", 37.5422, 127.0526, "상권/쇼핑"));
        spots.add(createSpot("송리단길·호수단길", 37.5091, 127.1051, "상권/쇼핑"));
        spots.add(createSpot("쌍문역", 37.6486, 127.0347, "상권/쇼핑"));
        spots.add(createSpot("압구정로데오거리", 37.5274, 127.0409, "상권/쇼핑"));
        spots.add(createSpot("여의도", 37.5216, 126.9242, "상권/쇼핑"));
        spots.add(createSpot("연남동", 37.5645, 126.9229, "상권/쇼핑"));
        spots.add(createSpot("영등포 타임스퀘어", 37.5172, 126.9034, "상권/쇼핑"));
        spots.add(createSpot("용리단길", 37.5305, 126.9692, "상권/쇼핑"));
        spots.add(createSpot("이태원 앤틱가구거리", 37.5332, 126.9972, "상권/쇼핑"));
        spots.add(createSpot("인사동", 37.5744, 126.9882, "역사/문화"));
        spots.add(createSpot("익선동", 37.5744, 126.9895, "역사/문화"));
        spots.add(createSpot("잠실롯데타워·석촌호수", 37.5126, 127.1025, "상권/쇼핑"));
        spots.add(createSpot("창동 신경제 중심지", 37.6532, 127.0475, "상권/쇼핑"));
        spots.add(createSpot("청담동 명품거리", 37.5249, 127.0435, "상권/쇼핑"));
        spots.add(createSpot("청량리 제기동 일대 전통시장", 37.5802, 127.0418, "상권/쇼핑"));
        spots.add(createSpot("해방촌·경리단길", 37.5411, 126.9873, "상권/쇼핑"));

        // 🌳 공원 / 한강공원 / 산 (40개)
        spots.add(createSpot("DDP(동대문디자인플라자)", 37.5668, 127.0094, "문화/여가"));
        spots.add(createSpot("DMC(디지털미디어시티)", 37.5776, 126.8916, "문화/여가"));
        spots.add(createSpot("강서한강공원", 37.5967, 126.8166, "문화/여가"));
        spots.add(createSpot("고척돔", 37.4982, 126.8672, "스포츠/복합공간"));
        spots.add(createSpot("광나루한강공원", 37.5488, 127.1219, "문화/여가"));
        spots.add(createSpot("광화문광장", 37.5716, 126.9768, "문화/여가"));
        spots.add(createSpot("국립중앙박물관·용산가족공원", 37.5239, 126.9804, "문화/여가"));
        spots.add(createSpot("난지한강공원", 37.5661, 126.8783, "문화/여가"));
        spots.add(createSpot("남산공원", 37.5512, 126.9882, "문화/여가"));
        spots.add(createSpot("노들섬", 37.5175, 126.9582, "문화/여가"));
        spots.add(createSpot("뚝섬한강공원", 37.5284, 127.0683, "문화/여가"));
        spots.add(createSpot("망원한강공원", 37.5558, 126.8938, "문화/여가"));
        spots.add(createSpot("반포한강공원", 37.5332, 126.9946, "문화/여가"));
        spots.add(createSpot("북서울꿈의숲", 37.6212, 127.0416, "문화/여가"));
        spots.add(createSpot("서리풀공원·몽마르뜨공원", 37.4952, 127.0062, "문화/여가"));
        spots.add(createSpot("서울대공원", 37.4275, 127.0166, "문화/여가"));
        spots.add(createSpot("서울숲공원", 37.5446, 127.0377, "문화/여가"));
        spots.add(createSpot("아차산", 37.5517, 127.1023, "문화/여가"));
        spots.add(createSpot("양화한강공원", 37.5383, 126.8973, "문화/여가"));
        spots.add(createSpot("어린이대공원", 37.5480, 127.0746, "문화/여가"));
        spots.add(createSpot("여의도한강공원", 37.5271, 126.9328, "문화/여가"));
        spots.add(createSpot("월드컵공원", 37.5638, 126.8906, "문화/여가"));
        spots.add(createSpot("응봉산", 37.5483, 127.0317, "문화/여가"));
        spots.add(createSpot("이촌한강공원", 37.5168, 126.9723, "문화/여가"));
        spots.add(createSpot("잠실종합운동장", 37.5148, 127.0736, "스포츠/복합공간"));
        spots.add(createSpot("잠실한강공원", 37.5178, 127.0861, "문화/여가"));
        spots.add(createSpot("잠원한강공원", 37.5383, 127.0216, "문화/여가"));
        spots.add(createSpot("청계산", 37.4445, 127.0561, "문화/여가"));

        android.util.Log.d("FIRESTORE_DUMP", "⏳ Firestore에 실데이터 전송 중...");

        for (Map<String, Object> spot : spots) {
            String name = (String) spot.get("area_name");
            db.collection("spots").document(name)
                    .set(spot)
                    .addOnFailureListener(e -> {
                        android.util.Log.e("FIRESTORE_DUMP", "❌ 저장 실패: " + name + " / 사유: " + e.getMessage());
                    });
        }
        android.util.Log.d("FIRESTORE_DUMP", "🎉 113개 데이터 전송 요청 완료!");
    }

    private static Map<String, Object> createSpot(String name, double lat, double lng, String category) {
        Map<String, Object> spot = new HashMap<>();
        spot.put("area_name", name);
        spot.put("lat", lat);
        spot.put("lng", lng);
        spot.put("category", category);
        return spot;
    }
}