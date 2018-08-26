package com.example.gentl.olearisweather.dataClasses;

public class Forecast
{
    int id_city, id_forecast;
    String description;
    String temp;
    String imagePath;
    String updateTime;

    public int getIdForecast()
    {
        return id_forecast;
    }

    public void setIdForecast(int id_forecast)
    {
        this.id_forecast = id_forecast;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getTemp()
    {
        return temp;
    }

    public void setTemp(String temp)
    {
        this.temp = temp;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    public int getIdCity()
    {
        return id_city;
    }

    public void setIdCity(int id_city)
    {
        this.id_city = id_city;
    }

    public String getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(String updateTime)
    {
        this.updateTime = updateTime;
    }

    public Forecast(int id_forecast, int id_city, String description, String temp, String imagePath, String updateTime)
    {
        this.id_forecast = id_forecast;
        this.id_city = id_city;
        this.description = description;
        this.temp = temp;
        this.imagePath = imagePath;
        this.updateTime = updateTime;
    }

    public Forecast()
    {
    }
}
