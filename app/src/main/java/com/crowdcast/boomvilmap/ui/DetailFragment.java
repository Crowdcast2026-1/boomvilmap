package com.crowdcast.boomvilmap.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.crowdcast.boomvilmap.model.PredictionResponse;
import com.crowdcast.boomvilmap.model.Spot;
import com.crowdcast.boomvilmap.model.WeatherResponse;
import com.crowdcast.boomvilmap.repository.FavoriteRepository;
import com.crowdcast.boomvilmap.repository.SpotRepository;
import com.crowdcast.boomvilmap.repository.WeatherRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
    private TextView congestionTitle;
    private TextView weatherSky;
    private TextView weatherWind;
    private TextView weatherHumidity;
    private TextView weeklyTitle;
    private TextView weeklyStatus;
    private EditText editDate;
    private TextView congestionMeta;
    private ProgressBar congestionProgress;
    private WeeklyCongestionChartView weeklyChart;
    private View weatherSection;
    private View weeklyPredictionSection;
    private boolean showingPrediction = false;

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
        setupDatePicker();
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
        congestionTitle = view.findViewById(R.id.text_congestion_title);
        weatherSky = view.findViewById(R.id.weather_sky);
        weatherWind = view.findViewById(R.id.weather_wind);
        weatherHumidity = view.findViewById(R.id.weather_humidity);
        weeklyTitle = view.findViewById(R.id.text_weekly_title);
        weeklyStatus = view.findViewById(R.id.text_weekly_status);
        editDate = view.findViewById(R.id.edit_date);
        congestionMeta = view.findViewById(R.id.text_congestion_meta);
        congestionProgress = view.findViewById(R.id.progress_congestion);
        weeklyChart = view.findViewById(R.id.chart_weekly);
        weatherSection = view.findViewById(R.id.layout_weather);
        weeklyPredictionSection = view.findViewById(R.id.layout_weekly_prediction);
    }

    private void setupDatePicker() {
        editDate.setFocusable(false);
        editDate.setOnClickListener(v -> showPredictionDatePicker());
    }

    private void showPredictionDatePicker() {
        Calendar tomorrow = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA);
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA);
                    selectedDate.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedDate.set(Calendar.MILLISECOND, 0);

                    String targetDate = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(selectedDate.getTime());
                    showPredictionTimePicker(targetDate);
                },
                tomorrow.get(Calendar.YEAR),
                tomorrow.get(Calendar.MONTH),
                tomorrow.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(tomorrow.getTimeInMillis());
        dialog.show();
    }

    private void showPredictionTimePicker(String targetDate) {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA);
        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String targetTime = String.format(Locale.KOREA, "%02d:%02d", hourOfDay, minute);
                    editDate.setText(targetDate + " " + targetTime);
                    loadPredictionForDateTime(targetDate, targetTime);
                },
                now.get(Calendar.HOUR_OF_DAY),
                0,
                true
        );
        dialog.setTitle("예측 시간 선택");
        dialog.show();
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

        loadDefaultWeeklyPrediction();
    }

    private void loadDefaultWeeklyPrediction() {
        if (weeklyTitle != null) {
            weeklyTitle.setText(getString(R.string.weekly_prediction));
        }
        setWeeklyStatus("이번 주 예측 불러오는 중...");

        SpotRepository.fetchWeeklyPrediction(currentSpot, new SpotRepository.OnWeeklyPredictionLoadedListener() {
            @Override
            public void onSuccess(List<Spot.Level> weeklyLevels, List<String> labels) {
                if (!isAdded()) return;
                renderWeeklyPrediction(weeklyLevels, labels, "이번 주");
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                weeklyChart.setData(new int[0], new int[0], new String[0]);
                setWeeklyStatus("이번 주 예측 실패: " + message);
            }
        });
    }

    private void loadPredictionForDateTime(String targetDate, String targetTime) {
        setPredictionMode(true);
        congestionMeta.setText("선택 날짜 예측 불러오는 중...");
        loadWeeklyPredictionForSelectedWeek(targetDate, targetTime);

        SpotRepository.fetchPrediction(currentSpot, targetDate, targetTime, new SpotRepository.OnPredictionLoadedListener() {
            @Override
            public void onSuccess(PredictionResponse prediction) {
                if (!isAdded()) return;
                renderDatePrediction(targetDate, targetTime, prediction);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                congestionMeta.setText("예측 실패: " + message);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWeeklyPredictionForSelectedWeek(String targetDate, String targetTime) {
        if (weeklyTitle != null) {
            weeklyTitle.setText("선택 주 혼잡도 예측");
        }
        setWeeklyStatus("선택한 주 예측 불러오는 중...");
        weeklyChart.setData(new int[0], new int[0], new String[0]);

        SpotRepository.fetchWeeklyPredictionForWeek(currentSpot, targetDate, targetTime, new SpotRepository.OnWeeklyPredictionLoadedListener() {
            @Override
            public void onSuccess(List<Spot.Level> weeklyLevels, List<String> labels) {
                if (!isAdded()) return;
                renderWeeklyPrediction(weeklyLevels, labels, "선택 주");
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                weeklyChart.setData(new int[0], new int[0], new String[0]);
                setWeeklyStatus("선택한 주 예측 실패: " + message);
            }
        });
    }

    private void renderSpot(Spot spot) {
        name.setText(valueOrDefault(spot.name, ""));
        region.setText(valueOrDefault(spot.region, "서울"));
        category.setText(valueOrDefault(spot.category, ""));
        description.setVisibility(View.GONE);
        if (showingPrediction) {
            renderHeroImage(spot);
            return;
        }

        renderCurrentCongestion(spot);
        renderHeroImage(spot);
    }

    private void renderCurrentCongestion(Spot spot) {
        if (spot.hasData) {
            visitors.setText(String.format(Locale.KOREA, "%,d명 방문", spot.visitors));
        } else {
            visitors.setText("- 명 방문");
        }
        congestionMeta.setText(sourceLabel(spot) + " · 기준 " + valueOrDefault(spot.sourceUpdatedAt, valueOrDefault(spot.observedAt, "-")));

        Spot.Level level = spot.hasData ? spot.level : null;
        levelIcon.setText(levelToEmoji(level) + "\n" + levelToLabel(level));
        levelIcon.setBackgroundResource(levelToBackground(level));
        levelIcon.setTextColor(ContextCompat.getColor(requireContext(), levelToTextColor(level)));

        congestionProgress.setProgress(levelToProgress(level));
        congestionProgress.setProgressTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), levelToProgressColor(level))
        ));
    }

    private void renderHeroImage(Spot spot) {
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

    private void renderWeeklyPrediction(List<Spot.Level> weeklyLevels, List<String> labels, String statusPrefix) {
        int size = weeklyLevels.size();
        if (size == 0) {
            weeklyChart.setData(new int[0], new int[0], new String[0]);
            setWeeklyStatus(statusPrefix + " 예측 데이터가 없습니다.");
            return;
        }

        if (size < 7) {
            setWeeklyStatus("일부 날짜만 표시 중 (" + size + "/7)");
        } else if (weeklyLevels.contains(null)) {
            setWeeklyStatus("일부 날짜는 예측 데이터가 없어 회색으로 표시됩니다.");
        } else {
            hideWeeklyStatus();
        }

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

    private void setWeeklyStatus(String message) {
        if (weeklyStatus == null) return;
        weeklyStatus.setText(message);
        weeklyStatus.setVisibility(View.VISIBLE);
    }

    private void hideWeeklyStatus() {
        if (weeklyStatus == null) return;
        weeklyStatus.setVisibility(View.GONE);
    }

    private void renderDatePrediction(String targetDate, String targetTime, PredictionResponse prediction) {
        Spot.Level predictedLevel = Spot.parseLevel(prediction.predicted_congestion_level);
        levelIcon.setText(levelToEmoji(predictedLevel) + "\n" + levelToLabel(predictedLevel));
        levelIcon.setBackgroundResource(levelToBackground(predictedLevel));
        levelIcon.setTextColor(ContextCompat.getColor(requireContext(), levelToTextColor(predictedLevel)));

        congestionProgress.setProgress(levelToProgress(predictedLevel));
        congestionProgress.setProgressTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), levelToProgressColor(predictedLevel))
        ));

        congestionMeta.setText(targetDate + " " + targetTime + " 예측");
    }

    private void setPredictionMode(boolean predictionMode) {
        showingPrediction = predictionMode;
        if (weatherSection != null) {
            weatherSection.setVisibility(predictionMode ? View.GONE : View.VISIBLE);
        }
        if (weeklyPredictionSection != null) {
            weeklyPredictionSection.setVisibility(View.VISIBLE);
        }
        if (congestionTitle != null) {
            congestionTitle.setText(predictionMode ? "예측 혼잡도" : getString(R.string.current_congestion));
        }
        if (visitors != null) {
            visitors.setVisibility(predictionMode ? View.GONE : View.VISIBLE);
        }
    }

    private String sourceLabel(Spot spot) {
        if (spot == null || "unavailable".equals(spot.dataSource)) return "데이터 없음";
        if ("database_fallback".equals(spot.dataSource)) return "최근 저장 데이터";
        return "실시간";
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
        if (level == null) return "-";
        switch (level) {
            case VERY_CROWDED: return "붐빔";
            case CROWDED: return "약간 붐빔";
            case NORMAL: return "보통";
            case FREE:
            default: return "여유";
        }
    }

    private String levelToEmoji(Spot.Level level) {
        if (level == null) return "-";
        switch (level) {
            case VERY_CROWDED: return "😫";
            case CROWDED: return "😐";
            case NORMAL: return "🙂";
            case FREE:
            default: return "😊";
        }
    }

    private int levelToBackground(Spot.Level level) {
        if (level == null) return R.drawable.bg_chip_gray;
        switch (level) {
            case VERY_CROWDED: return R.drawable.bg_high;
            case CROWDED: return R.drawable.bg_crowded;
            case NORMAL: return R.drawable.bg_moderate;
            case FREE:
            default: return R.drawable.bg_low;
        }
    }

    private int levelToTextColor(Spot.Level level) {
        if (level == null) return R.color.text_secondary;
        switch (level) {
            case VERY_CROWDED: return R.color.high_text;
            case CROWDED: return R.color.crowded_text;
            case NORMAL: return R.color.moderate_text;
            case FREE:
            default: return R.color.low_text;
        }
    }

    private int levelToProgressColor(Spot.Level level) {
        if (level == null) return R.color.text_muted;
        switch (level) {
            case VERY_CROWDED: return R.color.congestion_high;
            case CROWDED: return R.color.congestion_crowded;
            case NORMAL: return R.color.congestion_moderate;
            case FREE:
            default: return R.color.congestion_low;
        }
    }

    private int levelToProgress(Spot.Level level) {
        if (level == null) return 0;
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
