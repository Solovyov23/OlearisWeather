package com.example.gentl.olearisweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.example.gentl.olearisweather.Adapters.ForecastAdapter;
import com.example.gentl.olearisweather.dataClasses.City;
import com.example.gentl.olearisweather.helpful.NetworkHelper;

// Activity shows the history of the weather forecast for one, the current city
public class WeatherActivity extends AppCompatActivity implements View.OnClickListener
{
    private ForecastAdapter forecastAdapter;
    private City currentCity;
    TextView emptyListTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Get information about the city on which you want to show the weather forecast history
        currentCity = (City) getIntent().getSerializableExtra("city");
        setTitle(currentCity.getName());
        // Initialize the main elements
        init();
    }

    // Initialize the main elements
    private void init()
    {
        // By clicking on this button, a new weather forecast will be added
        Button bCheckWeather = (Button) findViewById(R.id.bCheckWeather);
        bCheckWeather.setOnClickListener(this);

        emptyListTextView = (TextView) findViewById(R.id.idEmptyListTextView);

        ////////////////ADAPTER

        forecastAdapter = new ForecastAdapter(this, new ForecastAdapter.DatabaseAdapter() {
            @Override
            public void loadDataFinished(int count)
            {
                if(count != 0) emptyListTextView.setVisibility(View.GONE);
                else Helper.animateViewVisibility(emptyListTextView);
            }
        }, currentCity);

        ListView listOfForecasts = (ListView) findViewById(R.id.listOfForecasts);
        listOfForecasts.setAdapter(forecastAdapter);

        ////////////////END
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.bCheckWeather:
            {
                // if the internet is there, then start downloading the weather for a given second
                if(currentCity != null && NetworkHelper.isOnline(getApplicationContext())) forecastAdapter.addForecast();
                else
                break;
            }
        }
    }
}
