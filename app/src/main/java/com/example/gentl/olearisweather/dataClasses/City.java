package com.example.gentl.olearisweather.dataClasses;

import java.io.Serializable;

public class City implements Serializable
{
    int id;
    String name, fullName;
    // Coordinates
    double latitude, longitude;

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public City (int id, String name, String fullName, double latitude, double longitude)
    {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public City() { }
}
