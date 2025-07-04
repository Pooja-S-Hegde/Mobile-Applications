package com.example.weatherforecast;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherModel {
    @SerializedName("name")
    private String name;

    @SerializedName("weather")
    private List<Weather> weather;

    @SerializedName("main")
    private Main main;

    @SerializedName("wind")
    private Wind wind;

    public String getName() { return name; }
    public List<Weather> getWeather() { return weather; }
    public Main getMain() { return main; }
    public Wind getWind() { return wind; }

    public static class Weather {
        @SerializedName("main")
        private String main;
        @SerializedName("description")
        private String description;
        @SerializedName("icon")
        private String icon;

        public String getMain() { return main; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }

    public static class Main {
        @SerializedName("temp")
        private float temp;
        @SerializedName("humidity")
        private int humidity;
        @SerializedName("pressure")
        private int pressure;

        public float getTemp() { return temp; }
        public int getHumidity() { return humidity; }
        public int getPressure() { return pressure; }
    }

    public static class Wind {
        @SerializedName("speed")
        private float speed;

        public float getSpeed() { return speed; }
    }
} 