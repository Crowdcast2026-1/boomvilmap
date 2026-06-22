package com.crowdcast.boomvilmap.model;

public class WeatherResponse {
    public CurrentWeather current;

    public static class CurrentWeather {
        public double temperature_2m;
        public int relative_humidity_2m;
        public double wind_speed_10m;
        public int weather_code;
    }
}
