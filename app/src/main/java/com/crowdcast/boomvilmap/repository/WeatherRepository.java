package com.crowdcast.boomvilmap.repository;

import com.crowdcast.boomvilmap.model.WeatherResponse;
import com.crowdcast.boomvilmap.network.RetrofitClient;
import com.crowdcast.boomvilmap.network.SeoulCrowdApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private static final String CURRENT_VARIABLES =
            "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m";

    public interface WeatherCallback {
        void onSuccess(WeatherResponse.CurrentWeather weather);
        void onError(String message);
    }

    public static void fetchCurrentWeather(double lat, double lng, WeatherCallback callback) {
        SeoulCrowdApiService api = RetrofitClient.getApiService();
        api.getCurrentWeather(lat, lng, CURRENT_VARIABLES, "Asia/Seoul", "ms")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().current == null) {
                            callback.onError("날씨 API 응답이 올바르지 않습니다.");
                            return;
                        }
                        callback.onSuccess(response.body().current);
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        callback.onError("날씨 API 호출 실패: " + t.getMessage());
                    }
                });
    }
}
