package com.crowdcast.boomvilmap.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.crowdcast.boomvilmap.R;
import com.crowdcast.boomvilmap.model.Spot;
import com.crowdcast.boomvilmap.model.WeatherResponse;
import com.crowdcast.boomvilmap.repository.FavoriteRepository;
import com.crowdcast.boomvilmap.repository.SpotRepository;
import com.crowdcast.boomvilmap.repository.WeatherRepository;

import java.util.List;
import java.util.Locale;

public class DetailFragment extends Fragment {
    private static final String ARG_SPOT_ID = "spot_id";

    private Spot currentSpot;
    private FavoriteRepository favoriteRepository;
    private boolean isFavorite = false;

    private ImageView imageHero;
    private ImageButton buttonBookmark;
    private TextView name;
    private TextView region;
    private TextView category;
    private TextView description;
    private TextView visitors;
    private TextView levelIcon;
    private TextView weatherSky;
    private TextView weatherWind;
    private TextView weatherHumidity;
    private ProgressBar congestionProgress;
    private WeeklyCongestionChartView weeklyChart;

    public static DetailFragment newInstance(int spotId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SPOT_ID, spotId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        int spotId = getArguments() != null ? getArguments().getInt(ARG_SPOT_ID, 0) : 0;
        currentSpot = SpotRepository.findById(spotId);
        favoriteRepository = new FavoriteRepository();

        bindViews(view);
        weeklyChart.setData(new int[0], new int[0], new String[0]);

        view.findViewById(R.id.button_back).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        if (currentSpot == null) {
            Toast.makeText(requireContext(), "관광지 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        renderSpot(currentSpot);
        setWeatherLoading();
        setupFavoriteButton();
        loadApiData();
    }

    private void bindViews(View view) {
        imageHero = view.findViewById(R.id.image_hero);
        buttonBookmark = view.findViewById(R.id.button_bookmark);
        name = view.findViewById(R.id.text_detail_name);
        region = view.findViewById(R.id.text_detail_region);
        category = view.findViewById(R.id.text_detail_category);
        description = view.findViewById(R.id.text_description);
        visitors = view.findViewById(R.id.text_visitors);
        levelIcon = view.findViewById(R.id.text_level_icon);
        weatherSky = view.findViewById(R.id.weather_sky);
        weatherWind = view.findViewById(R.id.weather_wind);
        weatherHumidity = view.findViewById(R.id.weather_humidity);
        congestionProgress = view.findViewById(R.id.progress_congestion);
        weeklyChart = view.findViewById(R.id.chart_weekly);
    }

    private void setupFavoriteButton() {
        updateFavoriteIcon();
        favoriteRepository.isFavorite(currentSpot.id, new FavoriteRepository.FavoriteStateCallback() {
            @Override
            public void onResult(boolean favorite) {
                if (!isAdded()) return;
                isFavorite = favorite;
                updateFavoriteIcon();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "즐겨찾기 상태 확인 실패", Toast.LENGTH_SHORT).show();
            }
        });

        buttonBookmark.setOnClickListener(v -> toggleFavorite());
    }

    private void toggleFavorite() {
        if (!favoriteRepository.isLoggedIn()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonBookmark.setEnabled(false);
        FavoriteRepository.ActionCallback callback = new FavoriteRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                isFavorite = !isFavorite;
                updateFavoriteIcon();
                buttonBookmark.setEnabled(true);
                Toast.makeText(requireContext(), isFavorite ? "즐겨찾기에 추가했습니다." : "즐겨찾기에서 제거했습니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                buttonBookmark.setEnabled(true);
                Toast.makeText(requireContext(), "즐겨찾기 처리 실패: " + message, Toast.LENGTH_SHORT).show();
            }
        };

        if (isFavorite) {
            favoriteRepository.removeFavorite(currentSpot.id, callback);
        } else {
            favoriteRepository.addFavorite(currentSpot, callback);
        }
    }

    private void updateFavoriteIcon() {
        if (buttonBookmark == null || getContext() == null) return;
        int color = isFavorite ? R.color.primary : R.color.text_primary;
        buttonBookmark.setColorFilter(ContextCompat.getColor(requireContext(), color));
    }

    private void loadApiData() {
        SpotRepository.fetchCurrentSpot(currentSpot, new SpotRepository.OnSpotLoadedListener() {
            @Override
            public void onSuccess(Spot spot) {
                if (!isAdded()) return;
                currentSpot = spot;
                renderSpot(spot);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        WeatherRepository.fetchCurrentWeather(currentSpot.lat, currentSpot.lng, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse.CurrentWeather weather) {
                if (!isAdded()) return;
                renderWeather(weather);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                weatherSky.setText("날씨\n실패");
                weatherWind.setText("바람\n-");
                weatherHumidity.setText("습도\n-");
            }
        });

        SpotRepository.fetchWeeklyPrediction(currentSpot, new SpotRepository.OnWeeklyPredictionLoadedListener() {
            @Override
            public void onSuccess(List<Spot.Level> weeklyLevels, List<String> labels) {
                if (!isAdded()) return;
                renderWeeklyPrediction(weeklyLevels, labels);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                weeklyChart.setData(new int[0], new int[0], new String[0]);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderSpot(Spot spot) {
        name.setText(valueOrDefault(spot.name, ""));
        region.setText(valueOrDefault(spot.region, "서울"));
        category.setText(valueOrDefault(spot.category, ""));
        description.setText(valueOrDefault(spot.description, "혼잡도 설명을 불러오는 중입니다."));
        visitors.setText(String.format(Locale.KOREA, "%,d명 방문", spot.visitors));

        Spot.Level level = spot.level != null ? spot.level : Spot.Level.FREE;
        levelIcon.setText(levelToEmoji(level) + "\n" + levelToLabel(level));
        levelIcon.setBackgroundResource(levelToBackground(level));
        levelIcon.setTextColor(ContextCompat.getColor(requireContext(), levelToTextColor(level)));

        congestionProgress.setProgress(levelToProgress(level));
        congestionProgress.setProgressTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), levelToProgressColor(level))
        ));

        String imageUrl = SpotRepository.resolveImageUrl(spot.name, spot.category, spot.imageUrl);
        Glide.with(imageHero.getContext())
                .load(imageUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .centerCrop()
                .into(imageHero);
    }

    private void setWeatherLoading() {
        weatherSky.setText("날씨\n로딩");
        weatherWind.setText("바람\n로딩");
        weatherHumidity.setText("습도\n로딩");
    }

    private void renderWeather(WeatherResponse.CurrentWeather weather) {
        weatherSky.setText(String.format(Locale.KOREA, "%s\n%.1f°C",
                weatherDescription(weather.weather_code),
                weather.temperature_2m
        ));
        weatherWind.setText(String.format(Locale.KOREA, "바람\n%.1fm/s", weather.wind_speed_10m));
        weatherHumidity.setText(String.format(Locale.KOREA, "습도\n%d%%", weather.relative_humidity_2m));
    }

    private void renderWeeklyPrediction(List<Spot.Level> weeklyLevels, List<String> labels) {
        int size = weeklyLevels.size();
        int[] values = new int[size];
        int[] colors = new int[size];
        String[] labelArray = new String[size];

        for (int i = 0; i < size; i++) {
            Spot.Level level = weeklyLevels.get(i);
            values[i] = levelToProgress(level);
            colors[i] = ContextCompat.getColor(requireContext(), levelToProgressColor(level));
            labelArray[i] = labels.get(i);
        }
        weeklyChart.setData(values, colors, labelArray);
    }

    private String weatherDescription(int weatherCode) {
        if (weatherCode == 0) return "맑음";
        if (weatherCode <= 3) return "구름";
        if (weatherCode == 45 || weatherCode == 48) return "안개";
        if (weatherCode >= 51 && weatherCode <= 67) return "비";
        if (weatherCode >= 71 && weatherCode <= 77) return "눈";
        if (weatherCode >= 80 && weatherCode <= 82) return "소나기";
        if (weatherCode >= 95) return "뇌우";
        return "날씨";
    }

    private String levelToLabel(Spot.Level level) {
        switch (level) {
            case VERY_CROWDED: return "붐빔";
            case CROWDED: return "약간 붐빔";
            case NORMAL: return "보통";
            case FREE:
            default: return "여유";
        }
    }

    private String levelToEmoji(Spot.Level level) {
        switch (level) {
            case VERY_CROWDED: return "😫";
            case CROWDED: return "😐";
            case NORMAL: return "🙂";
            case FREE:
            default: return "😊";
        }
    }

    private int levelToBackground(Spot.Level level) {
        switch (level) {
            case VERY_CROWDED: return R.drawable.bg_high;
            case CROWDED: return R.drawable.bg_crowded;
            case NORMAL: return R.drawable.bg_moderate;
            case FREE:
            default: return R.drawable.bg_low;
        }
    }

    private int levelToTextColor(Spot.Level level) {
        switch (level) {
            case VERY_CROWDED: return R.color.high_text;
            case CROWDED: return R.color.crowded_text;
            case NORMAL: return R.color.moderate_text;
            case FREE:
            default: return R.color.low_text;
        }
    }

    private int levelToProgressColor(Spot.Level level) {
        switch (level) {
            case VERY_CROWDED: return R.color.congestion_high;
            case CROWDED: return R.color.congestion_crowded;
            case NORMAL: return R.color.congestion_moderate;
            case FREE:
            default: return R.color.congestion_low;
        }
    }

    private int levelToProgress(Spot.Level level) {
        switch (level) {
            case VERY_CROWDED: return 95;
            case CROWDED: return 75;
            case NORMAL: return 50;
            case FREE:
            default: return 25;
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }
}
