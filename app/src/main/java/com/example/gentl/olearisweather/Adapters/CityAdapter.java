package com.example.gentl.olearisweather.Adapters;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.gentl.olearisweather.Helper;
import com.example.gentl.olearisweather.R;
import com.example.gentl.olearisweather.Retrofit.WeatherAPI;
import com.example.gentl.olearisweather.Retrofit.WeatherDay;
import com.example.gentl.olearisweather.dataClasses.City;
import com.example.gentl.olearisweather.dataClasses.DbHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// CityAdapter is needed for content management of ListView cities
// LoaderManager is needed to load data in another thread
public class CityAdapter extends CursorAdapter implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener
{
    private DbHelper db;
    // Keeps the count of cities in the list
    private int countData = -1;
    // Keeps the subscriber (where the class was called from) and calls his methods
    private DatabaseAdapter delegate;
    private int loaderId = 1;
    private Activity context;
    private LayoutInflater inflater;

    public CityAdapter(Activity context, DatabaseAdapter delegate)
    {
        super(context, null, 0);
        db = DbHelper.getInstance(context);
        this.context = context;
        this.delegate = delegate;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Load the data from the database into another thread
        context.getLoaderManager().restartLoader(loaderId, new Bundle(), this);
    }

    // Pull the City object out of the cursor
    public City getCityFromPosition(int position)
    {
        Cursor cursor = getCursor();
        City city = null;
        if(cursor.moveToPosition(position)) {
            city = new City();
            city.setId(cursor.getInt(cursor.getColumnIndex(DbHelper.CITIES_ID_CITY)));
            city.setName(cursor.getString(cursor.getColumnIndex(DbHelper.CITIES_NAME)));
            city.setFullName(cursor.getString(cursor.getColumnIndex(DbHelper.CITIES_FULL_NAME)));
            city.setLatitude(cursor.getDouble(cursor.getColumnIndex(DbHelper.CITIES_LATITUDE)));
            city.setLongitude(cursor.getDouble(cursor.getColumnIndex(DbHelper.CITIES_LONGITUDE)));
        }
        return city;
    }

    public void changeCursor()
    {
        try {
            context.getLoaderManager().getLoader(loaderId).forceLoad();
        } catch (Exception e) { }
    }

    // Send the request to the database to delete the city
    public void removeCity(City city)
    {
        DbHelper dbHelper = DbHelper.getInstance(context);
        dbHelper.removeCity(city.getId());
        changeCursor();
    }

    // Create an instance of the class, for further work, get the name of the place by coordinates
    WeatherAPI.ApiInterface api = WeatherAPI.getClient().create(WeatherAPI.ApiInterface.class);

    // Add a new city
    public void addCity(final String fullName, final double latitude, final double longitude)
    {
        String units = "metric";
        String key = WeatherAPI.KEY;

        // We address to the server, for reception of its version of a name of a city on coordinates
        // Since Google and the server can give different names of places
        Call<WeatherDay> callToday = api.getToday(latitude, longitude, units, key);
        callToday.enqueue(new Callback<WeatherDay>() {
            @Override
            public void onResponse(Call<WeatherDay> call, Response<WeatherDay> response) {
                WeatherDay data = response.body();

                if (response.isSuccessful())
                {
                    data.getDescription();
                    // If a city with that name already exists, then do not add it
                    if(db.addCity(new City(-1, data.getCity() + "", fullName, latitude, longitude)) == 404)
                    {
                        delegate.addDataFinished(false);
                    }
                    changeCursor();
                }
            }

            @Override
            public void onFailure(Call<WeatherDay> call, Throwable t) {
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return inflater.inflate(R.layout.item_city, parent, false);
    }

    //Fill elements with data
    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        final City city = getCityFromPosition(cursor.getPosition());
        View itemView = view.findViewById(R.id.id_layout_city);
        itemView.setTag(cursor.getPosition());
        itemView.setOnClickListener(this);

        ImageButton b_remove = view.findViewById(R.id.b_remove);
        b_remove.setTag(cursor.getPosition());
        b_remove.setOnClickListener(this);

        TextView tv_name = view.findViewById(R.id.tv_name);
        tv_name.setText(city.getName());

        TextView tv_fullName = view.findViewById(R.id.tv_fullName);
        tv_fullName.setText(city.getFullName());
    }

    // Process delete and click on list item
    @Override
    public void onClick(View view)
    {
        City city = getCityFromPosition((Integer)view.getTag());
        if (city == null) return;
        switch(view.getId())
        {
            case R.id.b_remove:
            {
                removeCity(city);
                break;
            }
            case R.id.id_layout_city:
            {
                // Open a new activity for the subscriber
                Helper.animateViewVisibility(view);
                delegate.openWeatherActivityCurrentCity(city);
                break;
            }
        }
    }

    //--------------------------------For background loading----------------------------

    private CityCursorLoader loader;
    // ---------------loader listener-------------
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        loader = new CityCursorLoader(context, db);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if(data == null) return;
        changeCursor(data);
        this.notifyDataSetChanged();
        if(data.getCount() != -1) delegate.loadDataFinished(data.getCount());
        countData = data.getCount();
    }

    public interface DatabaseAdapter{
        void loadDataFinished(int count);
        void addDataFinished(boolean isSuccess);
        void openWeatherActivityCurrentCity(City city);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        changeCursor(null);
        this.notifyDataSetChanged();
    }

    public DbHelper loaderGetDatabase()
    {
        return loader.getDatabase();
    }
}

class CityCursorLoader extends CursorLoader
{
    private DbHelper db;

    public CityCursorLoader(Context context, DbHelper db)
    {
        super(context);
        this.db = db;
    }

    // The method gets the data in the database in another thread
    @Override
    public Cursor loadInBackground()
    {
        Cursor cursor = null;
        cursor = db.getAllCities();
        return cursor;
    }

    public DbHelper getDatabase()
    {
        return db;
    }
}