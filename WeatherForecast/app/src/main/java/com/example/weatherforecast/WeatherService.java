package com.example.weatherforecast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("weather")
    Call<WeatherModel> getCurrentWeather(
        @Query("q") String city,
        @Query("appid") String apiKey,
        @Query("units") String units
    );

    @GET("forecast")
    Call<ForecastModel> getForecast(
        @Query("q") String city,
        @Query("appid") String apiKey,
        @Query("units") String units
    );
} 