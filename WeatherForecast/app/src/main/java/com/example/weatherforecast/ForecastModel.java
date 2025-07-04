package com.example.weatherforecast;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ForecastModel {
    @SerializedName("list")
    private List<ForecastItem> list;

    public List<ForecastItem> getList() { return list; }

    public static class ForecastItem {
        @SerializedName("dt_txt")
        private String dtTxt;
        @SerializedName("main")
        private WeatherModel.Main main;
        @SerializedName("weather")
        private List<WeatherModel.Weather> weather;

        public String getDtTxt() { return dtTxt; }
        public WeatherModel.Main getMain() { return main; }
        public List<WeatherModel.Weather> getWeather() { return weather; }
    }

    // Helper: Get one forecast per day (e.g., at 12:00)
    public static List<ForecastItem> getDailyForecasts(List<ForecastItem> all) {
        List<ForecastItem> daily = new ArrayList<>();
        String lastDate = "";
        for (ForecastItem item : all) {
            if (item.getDtTxt().contains("12:00:00")) {
                String date = item.getDtTxt().split(" ")[0];
                if (!date.equals(lastDate)) {
                    daily.add(item);
                    lastDate = date;
                }
            }
        }
        return daily;
    }
} 