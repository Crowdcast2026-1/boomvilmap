package com.crowdcast.boomvilmap.model;

import java.util.List;

public class CollectAllResponse {
    public int requested_area_count;
    public int returned_area_count;
    public int live_success_count;
    public int live_error_count;
    public int fallback_count;
    public int success_count;
    public int error_count;
    public int unavailable_count;
    public boolean is_complete_live;
    public boolean is_complete;
    public double live_deadline_seconds;
    public List<CurrentPopulationResponse> areas;
    public List<ApiError> errors;

    // 이전 collect/all 응답과의 임시 호환용
    public List<CurrentPopulationResponse> collected;

    public static class ApiError {
        public String area;
        public String error;
    }
}
