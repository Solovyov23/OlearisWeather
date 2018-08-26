package com.example.gentl.olearisweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gentl.olearisweather.Adapters.CityAdapter;
import com.example.gentl.olearisweather.dataClasses.City;
import com.example.gentl.olearisweather.helpful.NetworkHelper;
import com.google.android.gms.maps.model.LatLng;

// Main activity shows saved city
public class MainActivity extends AppCompatActivity
{
    // Warning if the list is empty
    private TextView emptyListTextView;
    // List of all cities
    private ListView listOfCities;
    // By clicking on it open the map to add a new city
    private FloatingActionButton fabAddNewCity;
    // To manage list of Cities data via adapter
    private CityAdapter cityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize the main elements
        init();
    }

    // Initialize the main elements
    private void init()
    {
        // The menu
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emptyListTextView = (TextView) findViewById(R.id.idEmptyListTextView);
        fabAddNewCity = (FloatingActionButton) findViewById(R.id.idFabAddNewCity);
        fabAddNewCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // Check the connection to the Internet before opening the maps
                if(!NetworkHelper.isOnline(getApplicationContext())) return;
                // Open the map and return from there the location data
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        //ADAPTER

        cityAdapter = new CityAdapter(this, new CityAdapter.DatabaseAdapter()
        {
            // Loading data from the database was completed
            @Override
            public void loadDataFinished(int count)
            {
                // If the list of cities is not empty, then remove the warning
                if(count != 0) emptyListTextView.setVisibility(View.GONE);
                else Helper.animateViewVisibility(emptyListTextView);
            }
            // Adding a new city was unsuccessfully
            public void addDataFinished(boolean isSuccess)
            {
                if(!isSuccess)
                {
                    Toast.makeText(getApplicationContext(), getString(R.string.city_already_exists), Toast.LENGTH_SHORT).show();
                }
            }
            // Open a window with the forecast history for the selected city
            public void openWeatherActivityCurrentCity(City city)
            {
                Intent newIntent = new Intent(getApplicationContext(), WeatherActivity.class);
                newIntent.putExtra("city", city);
                getApplicationContext().startActivity(newIntent);
            }
        });

        listOfCities = (ListView) findViewById(R.id.listOfCities);
        listOfCities.setAdapter(cityAdapter);

        // Adapter END
    }

    // Data returned from MapsActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (data == null) {return;}
        String name = data.getStringExtra("name");
        String full_name = data.getStringExtra("full_name");
        double latitude = data.getDoubleExtra("latitude", 0);
        double longitude = data.getDoubleExtra("longitude", 0);
        LatLng myPosition = new LatLng(latitude, longitude);

        cityAdapter.addCity(full_name, latitude, longitude);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
