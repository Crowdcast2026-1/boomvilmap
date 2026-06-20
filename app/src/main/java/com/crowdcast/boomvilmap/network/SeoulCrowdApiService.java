package com.crowdcast.boomvilmap.network;

import com.crowdcast.boomvilmap.model.CollectAllResponse;
import com.crowdcast.boomvilmap.model.CurrentPopulationResponse;
import com.crowdcast.boomvilmap.model.PredictionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SeoulCrowdApiService {

    // 1. 실시간 인구 및 혼잡도 조회 API
    @GET("population/current")
    Call<CurrentPopulationResponse> getCurrentPopulation(
            @Query("area") String areaName
    );

    // 2. 미래 날짜/시간대 혼잡도 AI 예측 API
    @GET("predictions")
    Call<PredictionResponse> getPrediction(
            @Query("area") String areaName,
            @Query("target_date") String targetDate, // 형식: YYYY-MM-DD
            @Query("target_time") String targetTime  // 형식: HH:MM
    );

    @POST("collect/all")
    Call<CollectAllResponse> getAllPopulation();
}