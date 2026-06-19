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
}