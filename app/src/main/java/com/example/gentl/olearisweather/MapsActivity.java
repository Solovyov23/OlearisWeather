package com.example.gentl.olearisweather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.gentl.olearisweather.Retrofit.WeatherAPI;
import com.example.gentl.olearisweather.Retrofit.WeatherDay;
import com.example.gentl.olearisweather.helpful.NetworkHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Activity allows you to select a city on the map
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener
{
    private GoogleMap googleMap;
    private Marker marker;
    private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Two main buttons
        Button bAddNewPlace = (Button) findViewById(R.id.bAddNewPlace);
        bAddNewPlace.setOnClickListener(this);
        Button bCancel = (Button) findViewById(R.id.bCancel);
        bCancel.setOnClickListener(this);

    }

    //implement the onClick method here
    public void onClick(View v) {
        // Perform action on click
        Intent intent = new Intent();
        switch(v.getId()) {
            case R.id.bAddNewPlace:

                // If the important data were empty, then
                if(marker != null || marker.getTitle() == "" || marker.getTag() == "")
                {
                    // Check your internet connection
                    NetworkHelper.isOnline(getApplicationContext());
                }
                // Return result to main activity
                intent.putExtra("name", marker.getTitle() + "");
                intent.putExtra("full_name", marker.getTag() + "");
                intent.putExtra("latitude", marker.getPosition().latitude);
                intent.putExtra("longitude", marker.getPosition().longitude);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.bCancel:
                setResult(RESULT_CANCELED, intent);
                finish();
                break;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap)
    {
        this.googleMap = googleMap;
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabledGPS = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean enabledWiFi = service
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabledGPS)
        {
            Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        if(checkSelfPermission(true)) init();
    }

    @SuppressLint("MissingPermission")
    public void init()
    {
        // Enabling MyLocation Layer of Google Map
        googleMap.setMyLocationEnabled(true);

        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);


        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null)
        {
            // Getting latitude of the current location
            double latitude = location.getLatitude();

            // Getting longitude of the current location
            double longitude = location.getLongitude();

            // Creating a LatLng object for the current location
            LatLng myPosition = new LatLng(latitude, longitude);

            googleMap.moveCamera( CameraUpdateFactory.newLatLngZoom(myPosition, 15));
            String title = getAddressFromPosition(myPosition);
            marker = googleMap.addMarker(new MarkerOptions().position(myPosition));
            marker.setTag(title);
        }

        // по клику на карте, передвинуть маркер с названием места
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
        {
            @Override
            public void onMapLongClick (LatLng newPosition)
            {
                if(marker != null) marker.remove();
                String title = getAddressFromPosition(newPosition);
                if(title == null) return;
                marker = googleMap.addMarker(new MarkerOptions().position(newPosition));
                marker.setTag(title);
            }
        });
    }

    //////////Permissions////////

    // Called when user response was made to permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        // If the user allowed geolocation, then
        if(checkSelfPermission(false)) init();
        else
        {
            // Otherwise close the window
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    private boolean checkSelfPermission(boolean requestPermissions)
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(requestPermissions) requestPermissions();
            return false;
        }
        return true;
    }

    private void requestPermissions()
    {
        ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                MY_PERMISSION_ACCESS_COURSE_LOCATION );
    }

    //////////Permissions end////////

    private String getAddressFromPosition(LatLng position)
    {
        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(position.latitude,
                    position.longitude, 1);
        } catch (IOException e) {
            return null;
        }

        String address = addresses.get(0).getAddressLine(0); //0 to obtain first possible address
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        //create your custom title
        String title = country + ", " + state;
        getCity(position.latitude, position.longitude);
        return title;
    }

    // We address to the server, for reception of its version of a name of a city on coordinates
    // since Google and the server can give different names of places
    public void getCity(Double lat, Double lng)
    {
        String units = "metric";
        String key = WeatherAPI.KEY;

        WeatherAPI.ApiInterface api = WeatherAPI.getClient().create(WeatherAPI.ApiInterface.class);

        // Get weather for today
        Call<WeatherDay> callToday = api.getToday(lat, lng, units, key);
        callToday.enqueue(new Callback<WeatherDay>() {
            @Override
            public void onResponse(Call<WeatherDay> call, Response<WeatherDay> response) {
                WeatherDay data = response.body();
                // If everything is successful, then save this city name as the main
                if (response.isSuccessful()) {
                    marker.setTitle(data.getCity() + " ");
                    marker.showInfoWindow();
                }
            }
            @Override
            public void onFailure(Call<WeatherDay> call, Throwable t) {
            }
        });
    }
}
