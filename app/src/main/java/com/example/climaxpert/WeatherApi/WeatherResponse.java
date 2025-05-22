package com.example.climaxpert.WeatherApi;

public class WeatherResponse {
    private Main main;
    private Weather[] weather;
    private String name; // Название города

    public static class Main {
        private double temp;
        private double feels_like;
        private int humidity;

        // Геттеры
        public double getTemp() { return temp; }
        public double getFeelsLike() { return feels_like; }
        public int getHumidity() { return humidity; }
    }

    public static class Weather {
        private String main;
        private String description;
        private String icon;

        // Геттеры
        public String getMain() { return main; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }

    // Геттеры
    public Main getMain() { return main; }
    public Weather[] getWeather() { return weather; }
    public String getName() { return name; }
}