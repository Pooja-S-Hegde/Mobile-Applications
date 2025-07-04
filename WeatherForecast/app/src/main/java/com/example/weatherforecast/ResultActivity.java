package com.example.weatherforecast;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import android.widget.Toast;
import android.view.animation.AlphaAnimation;

public class ResultActivity extends AppCompatActivity {
    private TextView tvCity, tvTemp, tvCondition, tvHumidity, tvPressure, tvWind;
    private ImageView ivWeatherIcon, ivBackground;
    private ProgressBar progressBar;
    private RecyclerView recyclerForecast;
    private ForecastAdapter forecastAdapter;

    private static final String API_KEY = "842002570870dd3f8c669ecae93fe29e"; // <-- Put your OpenWeatherMap API key here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        try {
            MaterialToolbar toolbar = findViewById(R.id.topAppBar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());

            tvCity = findViewById(R.id.tvCity);
            tvTemp = findViewById(R.id.tvTemp);
            tvCondition = findViewById(R.id.tvCondition);
            tvHumidity = findViewById(R.id.tvHumidity);
            tvPressure = findViewById(R.id.tvPressure);
            tvWind = findViewById(R.id.tvWind);
            ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
            ivBackground = findViewById(R.id.ivBackground);
            progressBar = findViewById(R.id.progressBar);
            recyclerForecast = findViewById(R.id.recyclerForecast);

            if (recyclerForecast != null)
                recyclerForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            String city = getIntent() != null ? getIntent().getStringExtra("city_name") : null;
            if (city == null || city.isEmpty()) {
                Toast.makeText(this, "No city name provided.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            fetchWeather(city);
            fetchForecast(city);
        } catch (Exception e) {
            Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void fetchWeather(String city) {
        progressBar.setVisibility(View.VISIBLE);
        WeatherService service = APIClient.getClient().create(WeatherService.class);
        Call<WeatherModel> call = service.getCurrentWeather(city, API_KEY, "metric");
        call.enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                progressBar.setVisibility(View.GONE);
                try {
                    if (response.isSuccessful() && response.body() != null &&
                        response.body().getWeather() != null && !response.body().getWeather().isEmpty() &&
                        response.body().getMain() != null && response.body().getWind() != null) {
                        WeatherModel weather = response.body();
                        if (tvCity != null) tvCity.setText(weather.getName() != null ? weather.getName() : "-");
                        if (tvTemp != null) tvTemp.setText(String.format("%.0f°C", weather.getMain().getTemp()));
                        if (tvCondition != null) tvCondition.setText(weather.getWeather().get(0).getDescription());
                        if (tvHumidity != null) tvHumidity.setText("Humidity: " + weather.getMain().getHumidity() + "%");
                        if (tvPressure != null) tvPressure.setText("Pressure: " + weather.getMain().getPressure() + " hPa");
                        if (tvWind != null) tvWind.setText("Wind: " + weather.getWind().getSpeed() + " m/s");

                        String iconUrl = "https://openweathermap.org/img/wn/" + weather.getWeather().get(0).getIcon() + "@4x.png";
                        if (ivWeatherIcon != null) {
                            Glide.with(ResultActivity.this).load(iconUrl).into(ivWeatherIcon);
                            // Fade-in animation
                            ivWeatherIcon.setAlpha(0f);
                            ivWeatherIcon.animate().alpha(1f).setDuration(800).start();
                        }

                        setWeatherBackground(weather.getWeather().get(0).getMain());
                    } else {
                        showError("City not found or API error.");
                        Toast.makeText(ResultActivity.this, "City not found or API error.", Toast.LENGTH_LONG).show();
                        Log.e("WeatherAPI", "Response unsuccessful or body is null: " + response.message());
                    }
                } catch (Exception e) {
                    showError("Unexpected error occurred.");
                    Toast.makeText(ResultActivity.this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("WeatherAPI", "Exception in onResponse", e);
                }
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Network error. Please try again.");
                Toast.makeText(ResultActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("WeatherAPI", "onFailure", t);
            }
        });
    }

    private void fetchForecast(String city) {
        try {
            WeatherService service = APIClient.getClient().create(WeatherService.class);
            Call<ForecastModel> call = service.getForecast(city, API_KEY, "metric");
            call.enqueue(new Callback<ForecastModel>() {
                @Override
                public void onResponse(Call<ForecastModel> call, Response<ForecastModel> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null && response.body().getList() != null) {
                            List<ForecastModel.ForecastItem> forecastList = ForecastModel.getDailyForecasts(response.body().getList());
                            if (recyclerForecast != null) {
                                forecastAdapter = new ForecastAdapter(forecastList);
                                recyclerForecast.setAdapter(forecastAdapter);
                            }
                        } else {
                            showError("Failed to load forecast.");
                            Toast.makeText(ResultActivity.this, "Failed to load forecast.", Toast.LENGTH_LONG).show();
                            Log.e("ForecastAPI", "Response unsuccessful or body is null: " + response.message());
                        }
                    } catch (Exception e) {
                        showError("Unexpected error occurred in forecast.");
                        Toast.makeText(ResultActivity.this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("ForecastAPI", "Exception in onResponse", e);
                    }
                }

                @Override
                public void onFailure(Call<ForecastModel> call, Throwable t) {
                    showError("Failed to load forecast.");
                    Toast.makeText(ResultActivity.this, "Forecast network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("ForecastAPI", "onFailure", t);
                }
            });
        } catch (Exception e) {
            showError("Unexpected error occurred in forecast setup.");
            Toast.makeText(ResultActivity.this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ForecastAPI", "Exception in fetchForecast setup", e);
        }
    }

    private void showError(String message) {
        Snackbar.make(tvCity, message, Snackbar.LENGTH_LONG).show();
    }

    private void setWeatherBackground(String main) {
        int resId = R.drawable.bg_clear;
        if (main.equalsIgnoreCase("Clouds")) resId = R.drawable.bg_clouds;
        else if (main.equalsIgnoreCase("Rain")) resId = R.drawable.bg_rain;
        else if (main.equalsIgnoreCase("Snow")) resId = R.drawable.bg_snow;
        else if (main.equalsIgnoreCase("Thunderstorm")) resId = R.drawable.bg_thunder;
        else if (main.equalsIgnoreCase("Drizzle")) resId = R.drawable.bg_drizzle;
        else if (main.equalsIgnoreCase("Mist") || main.equalsIgnoreCase("Fog")) resId = R.drawable.bg_mist;
        Glide.with(this).load(resId).into(ivBackground);
    }

    private void fadeInView(View view) {
        if (view != null) {
            AlphaAnimation anim = new AlphaAnimation(0f, 1f);
            anim.setDuration(700);
            view.startAnimation(anim);
        }
    }
} 