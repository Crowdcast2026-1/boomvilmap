package com.crowdcast.boomvilmap.model;

public class CurrentPopulationResponse {
    public String area_name;
    public String area_code;
    public String observed_at;
    public String source_updated_at;
    public String congestion_level;
    public String congestion_message;
    public Integer population_min;
    public Integer population_max;
    public Double population_midpoint;
    public Double male_rate;
    public Double female_rate;
    public Double resident_rate;
    public Double non_resident_rate;
    public String data_source;
    public Boolean has_data;
    public String live_error;
}
