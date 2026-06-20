package com.crowdcast.boomvilmap.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8000/";
    private static Retrofit retrofit = null;

    public static SeoulCrowdApiService getApiService() {
        if (retrofit == null) {
            // ⭐ 대기 시간을 30초로 늘려주는 옵션 추가
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // 위에서 설정한 타임아웃 적용
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(SeoulCrowdApiService.class);
    }
}