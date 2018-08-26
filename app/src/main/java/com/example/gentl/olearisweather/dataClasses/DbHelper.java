package com.example.gentl.olearisweather.dataClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


// DbHelper is needed to work with the database
public class DbHelper extends SQLiteOpenHelper
{
    // Basic properties
    final static String databaseName = "weathermap.s3db";
    static String folderPath = "";
    final static int databaseVersion = 1;
    String outFullDatabaseFileName;
    Context appContext;

    public static final String KEY_ROWID = "rowid _id";

    // Field keys for the CITIES table
    private static final String CITIES_TABLE = "Cities";

    public static final String CITIES_ID_CITY = "id_city";
    // The name of the city name
    public static final String CITIES_NAME = "name";
    public static final String CITIES_FULL_NAME = "full_name";
    // Coordinates of the city
    public static final String CITIES_LATITUDE = "latitude";
    public static final String CITIES_LONGITUDE = "longitude";

    // Field keys for table FORECAST
    private static final String FORECAST_TABLE = "Forecasts";

    public static final String FORECAST_ID_FORECAST = "id_forecast";
    public static final String FORECAST_ID_CITY = "id_city";
    public static final String FORECAST_DESCRIPTION = "description";
    public static final String FORECAST_TEMP = "temp";
    // Stores the path to url pictures
    public static final String FORECAST_IMAGE = "image";
    // Stores the time of adding the weather forecast
    public static final String FORECAST_UPDATE_TIME = "update_time";

    private static DbHelper mInstance;
    private SQLiteDatabase db;

    // Return a single initialized instance of the class
    public static DbHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DbHelper(context);
        }
        return mInstance;
    }

    // Database creation
    private DbHelper(Context context)
    {
        super(context, databaseName, null, databaseVersion);
        appContext = context;
        folderPath = appContext.getFilesDir().getPath();
        try {
            copyDataBase(databaseName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Copying from the assets folder to the folder for work
    private void copyDataBase(String dbname) throws IOException
    {
        // open the database in assets
        InputStream myInput = appContext.getAssets().open(dbname);

        File db_folder = new File(folderPath);
        if(!db_folder.exists())
            db_folder.mkdir();

        outFullDatabaseFileName =  folderPath + "/" + dbname;

        if(new File(outFullDatabaseFileName).exists() && new File(outFullDatabaseFileName).length() != 0)
        {
            openDataBase(outFullDatabaseFileName);
        }
        else
        {
            // path to the database in the databases folder
            File file = new File(outFullDatabaseFileName);
            if(!file.exists()) file.createNewFile();

            OutputStream myOutput = new FileOutputStream(outFullDatabaseFileName);
            byte[] buffer = new byte[102400];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
            openDataBase(outFullDatabaseFileName);
        }
    }

    public boolean isOpen() {
        return db != null && db.isOpen();
    }

    public void close() {
        if (isOpen()) {
        }
    }

    private void openDataBase(String path)
    {
        if (!isOpen()) {
            db = SQLiteDatabase.openDatabase(path, null, 0);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /////////////////////Requests for the City table/////////////////////////////////////

    public Cursor getAllCities()
    {
        Cursor cursor = db.query(CITIES_TABLE, new String[] {
                        KEY_ROWID,
                        CITIES_ID_CITY,
                        CITIES_NAME,
                        CITIES_FULL_NAME,
                        CITIES_LATITUDE,
                        CITIES_LONGITUDE
                },
                null, null, null, null, CITIES_ID_CITY + " ASC"); //DESC

        //  Move the cursor to the first line of the query result
        if (cursor != null) cursor.moveToFirst();
        return cursor;
    }

    public long addCity(City city)
    {
        // Check if there is such a city already in the added,
        // if so, do not add it and tell the user about it
        if(dataExists(city.getName(), city.getFullName())) return 404;

        ContentValues initialValues = new ContentValues();
        initialValues.put(CITIES_NAME, city.getName());
        initialValues.put(CITIES_FULL_NAME, city.getFullName());
        initialValues.put(CITIES_LATITUDE, city.getLatitude());
        initialValues.put(CITIES_LONGITUDE, city.getLongitude());

        return db.insertWithOnConflict(CITIES_TABLE, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int removeCity(int id)
    {
        return db.delete(CITIES_TABLE, CITIES_ID_CITY + "=?", new String[]{Integer.toString(id)});
    }

    // Check if such city exists already in the added
    public boolean dataExists(String name, String fullname)
    {
        Cursor cursor = db.query(
                CITIES_TABLE,
                new String[] {
                        CITIES_NAME,
                        CITIES_FULL_NAME},
                CITIES_NAME + "= '" + name + "' AND " + CITIES_FULL_NAME + " = '" + fullname + "'",
                null, null, null, CITIES_NAME + " ASC");

        if (cursor.getCount() <= 0)
        {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }


    //////////////////////Requests for FORECAST/////////////////////////////////////

    public Cursor getAllForecasts(int idCurrentCity)
    {
        Cursor cursor = db.query(
                FORECAST_TABLE,
                new String[] {KEY_ROWID,
                        FORECAST_ID_FORECAST,
                        FORECAST_ID_CITY,
                        FORECAST_DESCRIPTION,
                        FORECAST_TEMP,
                        FORECAST_IMAGE,
                        FORECAST_UPDATE_TIME},
                FORECAST_ID_CITY + "=" + idCurrentCity,
                null, null, null, FORECAST_ID_FORECAST + " ASC");

        //  Move the cursor to the first line of the query result
        if (cursor != null) cursor.moveToFirst();
        return cursor;
    }

    public long addForecast(Forecast forecast)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(FORECAST_ID_CITY, forecast.getIdCity());
        initialValues.put(FORECAST_DESCRIPTION, forecast.getDescription());
        initialValues.put(FORECAST_TEMP, forecast.getTemp());
        initialValues.put(FORECAST_IMAGE, forecast.getImagePath());
        initialValues.put(FORECAST_UPDATE_TIME, forecast.getUpdateTime());

        return db.insertWithOnConflict(FORECAST_TABLE, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int removeForecast(int id)
    {
        return db.delete(FORECAST_TABLE, FORECAST_ID_FORECAST + "=?", new String[]{Integer.toString(id)});
    }
}
