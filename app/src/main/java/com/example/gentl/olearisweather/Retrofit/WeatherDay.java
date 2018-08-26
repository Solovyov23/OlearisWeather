package com.example.gentl.olearisweather.Retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.List;

// We will describe the data model for the weather forecast for the current day as Retrofit wishes.
public class WeatherDay
{
    public class WeatherTemp {
        Double temp;
        Double temp_min;
        Double temp_max;
    }

    public class WeatherDescription {
        String icon;
        String description;
    }

    @SerializedName("main")
    private WeatherTemp temp;

    @SerializedName("weather")
    private List<WeatherDescription> description;

    @SerializedName("name")
    private String city;

    @SerializedName("dt")
    private long timestamp;

    public WeatherDay(WeatherTemp temp, List<WeatherDescription> description) {
        this.temp = temp;
        this.description = description;
    }

    public Calendar getDate() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp * 1000);
        return date;
    }

    public String getTemp() { return String.valueOf(temp.temp); }

    public String getTempMin() { return String.valueOf(temp.temp_min); }

    public String getTempMax() { return String.valueOf(temp.temp_max); }

    public String getTempInteger() { return String.valueOf(temp.temp.intValue()); }

    public  String getDescription() { return description.get(0).description; }

    public String getTempWithDegree() { return String.valueOf(temp.temp.intValue()) + "\u00B0"; }

    public String getCity() { return city; }

    public String getIcon() { return description.get(0).icon; }

    public String getIconUrl() {
        return "http://openweathermap.org/img/w/" + description.get(0).icon + ".png";
    }
}