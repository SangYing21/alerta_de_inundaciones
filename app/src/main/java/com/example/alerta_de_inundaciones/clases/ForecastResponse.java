package com.example.alerta_de_inundaciones.clases;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ForecastResponse {
    @SerializedName("list")
    private List<Forecast> forecastList;

    public List<Forecast> getForecastList() {
        return forecastList;
    }

    public static class Forecast {
        @SerializedName("dt")
        private long dt;

        @SerializedName("main")
        private MainData mainData;

        @SerializedName("weather")
        private List<WeatherData> weatherDataList;

        public long getDt() {
            return dt;
        }

        public MainData getMainData() {
            return mainData;
        }

        public List<WeatherData> getWeatherDataList() {
            return weatherDataList;
        }
    }

    public static class MainData {
        @SerializedName("temp")
        private double temperature;

        @SerializedName("humidity")
        private int humidity;

        public double getTemperature() {
            return temperature;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public static class WeatherData {
        @SerializedName("description")
        private String description;

        public String getDescription() {
            return description;
        }
    }
}
