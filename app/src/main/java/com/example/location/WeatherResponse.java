package com.example.location;


import java.util.List;

public class WeatherResponse {
    private Main main;
    private List<Weather> weather;
    private String name;

    public Main getMain() { return main; }
    public List<Weather> getWeather() { return weather; }
    public String getName() { return name; }

    public class Main {
        private float temp;
        public float getTemp() { return temp; }
    }

    public class Weather {
        private String description;
        public String getDescription() { return description; }
    }
}
