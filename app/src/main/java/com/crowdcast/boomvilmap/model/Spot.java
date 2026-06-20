package com.crowdcast.boomvilmap.model;

public class Spot {
    //여유, 보통, 약간붐빔, 붐빔
    public enum Level {FREE,NORMAL,CROWDED,VERY_CROWDED}

    public final int id;
    public final String name;
    public final String region;
    public final String category;
    public final String imageUrl;
    public final Level level;
    public final int visitors;
    public final String description;
    public final double lat;
    public final double lng;

    public Spot(int id, String name, String region, String category, String imageUrl,
                Level level, int visitors, String description, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.category = category;
        this.imageUrl = imageUrl;
        this.level = level;
        this.visitors = visitors;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
    }

    public static Level parseLevel(String levelString) {
        if (levelString == null) return Level.FREE;
        String normalized = levelString.trim().toUpperCase().replace(" ", "_");
        switch (normalized) {
            case "VERY_CROWDED":
            case "HIGH":
            case "붐빔": return Level.VERY_CROWDED;
            case "CROWDED":
            case "약간_붐빔":
            case "약간붐빔": return Level.CROWDED;
            case "NORMAL":
            case "MODERATE":
            case "보통": return Level.NORMAL;
            case "FREE":
            case "LOW":
            case "여유":
            default: return Level.FREE;
        }
    }
}
