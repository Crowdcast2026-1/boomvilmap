package com.crowdcast.boomvilmap.model;

import java.util.Map;

public class PredictionResponse {
    public String area_name;
    public String target_datetime;
    public String predicted_congestion_level;
    public double confidence;
    public Map<String, Double> probabilities;
    public String model_trained_at;
    public String device;
}