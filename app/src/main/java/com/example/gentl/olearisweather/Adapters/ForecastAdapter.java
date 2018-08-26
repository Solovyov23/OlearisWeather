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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.gentl.olearisweather.R;
import com.example.gentl.olearisweather.Retrofit.WeatherAPI;
import com.example.gentl.olearisweather.Retrofit.WeatherDay;
import com.example.gentl.olearisweather.dataClasses.City;
import com.example.gentl.olearisweather.dataClasses.DbHelper;
import com.example.gentl.olearisweather.dataClasses.Forecast;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// ForecastAdapter is needed to manage the content of the ListView weather forecasts for the current, selected city
// LoaderManager is needed to load data in another thread
public class ForecastAdapter extends CursorAdapter implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener
{
    private DbHelper db;
    // Stores the number of forecasts in the list
    private int countData = -1;
    // Keeps the subscriber (where the class was called from) and calls methods
    private ForecastAdapter.DatabaseAdapter delegate;
    private int loaderId = 2;
    private Activity context;
    private LayoutInflater inflater;
    private City currentCity;

    public ForecastAdapter(Activity context, ForecastAdapter.DatabaseAdapter delegate, City currentCity)
    {
        super(context, null, 0);
        db = DbHelper.getInstance(context);
        this.context = context;
        this.delegate = delegate;
        this.currentCity = currentCity;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // load the data from the database into another thread
        context.getLoaderManager().restartLoader(loaderId, new Bundle(), this);
    }

    // Pull the Forecast object out of the cursor
    public Forecast getForecastFromPosition(int position)
    {
        Cursor cursor = getCursor();
        Forecast forecast = null;
        if(cursor.moveToPosition(position)) {
            forecast = new Forecast();
            forecast.setIdForecast(cursor.getInt(cursor.getColumnIndex(DbHelper.FORECAST_ID_FORECAST)));
            forecast.setIdCity(cursor.getInt(cursor.getColumnIndex(DbHelper.FORECAST_ID_CITY)));
            forecast.setDescription(cursor.getString(cursor.getColumnIndex(DbHelper.FORECAST_DESCRIPTION)));
            forecast.setTemp(cursor.getString(cursor.getColumnIndex(DbHelper.FORECAST_TEMP)));
            forecast.setImagePath(cursor.getString(cursor.getColumnIndex(DbHelper.FORECAST_IMAGE)));
            forecast.setUpdateTime(cursor.getString(cursor.getColumnIndex(DbHelper.FORECAST_UPDATE_TIME)));

        }
        return forecast;
    }

    public void changeCursor()
    {
        try {
            context.getLoaderManager().getLoader(loaderId).forceLoad();
        } catch (Exception e) {}
    }

    // Send a query to the database to delete one of the weather forecasts
    public void removeForecast(Forecast forecast)
    {
        DbHelper dbHelper = DbHelper.getInstance(context);
        dbHelper.removeForecast(forecast.getIdForecast());
        changeCursor();
    }

    // Create an instance of the class, for further work to get the weather report
    WeatherAPI.ApiInterface api = WeatherAPI.getClient().create(WeatherAPI.ApiInterface.class);

    // Add a new weather forecast
    public void addForecast()
    {
        String units = "metric";
        String key = WeatherAPI.KEY;

        // Get weather for today
        Call<WeatherDay> callToday = api.getToday(currentCity.getLatitude(), currentCity.getLongitude(), units, key);
        callToday.enqueue(new Callback<WeatherDay>() {
            @Override
            public void onResponse(Call<WeatherDay> call, Response<WeatherDay> response) {
                WeatherDay data = response.body();

                if (response.isSuccessful())
                {
                    // Get the current time
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    String currentDateandTime = sdf.format(new Date());
                    // Send the query to the database to add a new city with such properties
                    db.addForecast(new Forecast(-1,
                            currentCity.getId(),
                            data.getDescription(),
                            data.getTempWithDegree(),
                            data.getIconUrl(),
                            currentDateandTime));
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
        return inflater.inflate(R.layout.item_forecast, parent, false);
    }

    // Fill elements with data
    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        final Forecast forecast = getForecastFromPosition(cursor.getPosition());

        ImageButton b_remove = view.findViewById(R.id.b_remove);
        b_remove.setTag(cursor.getPosition());
        b_remove.setOnClickListener(this);

        TextView tv_description = view.findViewById(R.id.tv_description);
        tv_description.setText(forecast.getDescription());

        TextView tv_temp = view.findViewById(R.id.tv_temp);
        tv_temp.setText(forecast.getTemp());



        TextView tv_time = view.findViewById(R.id.tv_time);
        tv_time.setText(forecast.getUpdateTime());

        // Images for the weather will be loaded using the Glide library
        ImageView ivImage = view.findViewById(R.id.ivImage);
        Glide.with(context).load(forecast.getImagePath()).into(ivImage);
    }

    // Handle removal
    @Override
    public void onClick(View view)
    {
        Forecast forecast = getForecastFromPosition((Integer)view.getTag());
        if (forecast == null) return;
        switch(view.getId())
        {
            case R.id.b_remove:
            {
                removeForecast(forecast);
                break;
            }
        }
    }

    //--------------------------------For background loading----------------------------

    private ForecastCursorLoader loader;
    // ---------------loader listener-------------
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        loader = new ForecastCursorLoader(context, db, currentCity.getId());
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

class ForecastCursorLoader extends CursorLoader
{
    private DbHelper db;
    private int idCurrentCity;
    public ForecastCursorLoader(Context context, DbHelper db, int idCurrentCity)
    {
        super(context);
        this.db = db;
        this.idCurrentCity = idCurrentCity;
    }

    // The method gets the data in the database in another thread
    @Override
    public Cursor loadInBackground()
    {
        Cursor cursor = null;
        cursor = db.getAllForecasts(idCurrentCity);
        return cursor;
    }

    public DbHelper getDatabase()
    {
        return db;
    }
}