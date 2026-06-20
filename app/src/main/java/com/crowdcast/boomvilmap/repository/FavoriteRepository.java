package com.crowdcast.boomvilmap.repository;

import com.crowdcast.boomvilmap.model.Spot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public FavoriteRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface FavoriteStateCallback {
        void onResult(boolean isFavorite);
        void onError(String message);
    }

    public interface FavoritesCallback {
        void onSuccess(List<Spot> favorites);
        void onError(String message);
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void isFavorite(int spotId, FavoriteStateCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(false);
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("favorites")
                .document(String.valueOf(spotId))
                .get()
                .addOnSuccessListener(snapshot -> callback.onResult(snapshot.exists()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addFavorite(Spot spot, ActionCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("로그인이 필요합니다.");
            return;
        }
        if (spot == null) {
            callback.onError("관광지 정보를 찾을 수 없습니다.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", spot.id);
        data.put("name", spot.name);
        data.put("region", spot.region);
        data.put("category", spot.category);
        data.put("imageUrl", SpotRepository.resolveImageUrl(spot.name, spot.category, spot.imageUrl));
        data.put("level", spot.level != null ? spot.level.name() : Spot.Level.FREE.name());
        data.put("visitors", spot.visitors);
        data.put("description", spot.description);
        data.put("lat", spot.lat);
        data.put("lng", spot.lng);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(user.getUid())
                .collection("favorites")
                .document(String.valueOf(spot.id))
                .set(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void removeFavorite(int spotId, ActionCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("로그인이 필요합니다.");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("favorites")
                .document(String.valueOf(spotId))
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void loadFavorites(FavoritesCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("favorites")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Spot> favorites = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Spot storedSpot = spotFromDocument(document);
                        if (storedSpot == null) continue;

                        Spot latestSpot = SpotRepository.findByIdOrNull(storedSpot.id);
                        favorites.add(latestSpot != null ? latestSpot : storedSpot);
                    }
                    callback.onSuccess(favorites);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private Spot spotFromDocument(DocumentSnapshot document) {
        Long idLong = document.getLong("id");
        String name = document.getString("name");
        if (idLong == null || name == null) return null;

        String region = valueOrDefault(document.getString("region"), "서울");
        String category = valueOrDefault(document.getString("category"), "");
        String imageUrl = SpotRepository.resolveImageUrl(name, category, document.getString("imageUrl"));
        String description = valueOrDefault(document.getString("description"), "");

        Long visitorsLong = document.getLong("visitors");
        int visitors = visitorsLong != null ? visitorsLong.intValue() : 0;

        Double latObj = document.getDouble("lat");
        Double lngObj = document.getDouble("lng");
        double lat = latObj != null ? latObj : 0.0;
        double lng = lngObj != null ? lngObj : 0.0;

        Spot.Level level = parseStoredLevel(document.getString("level"));
        return new Spot(idLong.intValue(), name, region, category, imageUrl, level, visitors, description, lat, lng);
    }

    private Spot.Level parseStoredLevel(String levelString) {
        if (levelString == null) return Spot.Level.FREE;
        try {
            return Spot.Level.valueOf(levelString);
        } catch (IllegalArgumentException ignored) {
            return Spot.parseLevel(levelString);
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }
}
