package com.example.alerta_de_inundaciones.clases;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {
    @SerializedName("main")
    private MainData mainData;

    @SerializedName("wind")
    private WindData windData;

    @SerializedName("weather")
    private List<WeatherData> weatherDataList;

    public MainData getMainData() {
        return mainData;
    }

    public WindData getWindData() {
        return windData;
    }

    public List<WeatherData> getWeatherDataList() {
        return weatherDataList;
    }

    public static class MainData {
        @SerializedName("temp")
        private double temperature;

        @SerializedName("humidity")
        private int humidity;

        @SerializedName("description")
        private String description;

        public double getTemperature() {
            return temperature;
        }

        public int getHumidity() {
            return humidity;
        }

        public String getDescription() {
            return description;
        }
    }


    public static class WindData {
        @SerializedName("speed")
        private double speed;

        public double getSpeed() {
            return speed;
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

