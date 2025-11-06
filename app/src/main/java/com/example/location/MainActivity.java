package com.example.location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.DefaultRetryPolicy;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView tempTxt, cityTxt, weatherTxt;
    ImageView weatherIcon;

    private static final int LOCATION_PERMISSION_CODE = 1;
    String apiKey = "051bae09167cafbfe98aca6411b567c8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempTxt = findViewById(R.id.tempText);
        cityTxt = findViewById(R.id.locationText);

        weatherIcon = findViewById(R.id.weatherIcon);

        checkRequirementsAndFetch();
    }

    private void checkRequirementsAndFetch() {

        if (!isInternetAvailable()) {
            showInternetDialog();
            return;
        }

        if (!isLocationEnabled()) {
            showLocationDialog();
            return;
        }

        requestLocationPermission();
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return net != null && net.isConnected();
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Internet Required")
                .setMessage("Please enable internet to use this app")
                .setPositiveButton("Enable", (dialog, which) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton("Exit", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLocationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Required")
                .setMessage("Please enable GPS to fetch weather")
                .setPositiveButton("Enable", (dialog, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (loc != null) {
            fetchWeather(loc.getLatitude(), loc.getLongitude());
        } else {
            Toast.makeText(this, "Getting location...!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWeather(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon +
                "&appid=" + apiKey + "&units=metric";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {

                        JSONObject main = response.getJSONObject("main");
                        JSONArray weather = response.getJSONArray("weather");
                        JSONObject obj = weather.getJSONObject(0);

                        String temp = main.getString("temp");
                        String city = response.getString("name");
                        String cond = obj.getString("main");

                        tempTxt.setText(temp + "°C");
                        cityTxt.setText(city);
                        weatherTxt.setText(cond);

                        setWeatherIcon(cond);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                5000, 2, 1.5f));
        queue.add(request);
    }

    private void setWeatherIcon(String cond) {
        cond = cond.toLowerCase();

        if (cond.contains("cloud")) weatherIcon.setImageResource(R.drawable.cloud);
        else if (cond.contains("rain")) weatherIcon.setImageResource(R.drawable.rain);
        else if (cond.contains("sun") || cond.contains("clear")) weatherIcon.setImageResource(R.drawable.sun);
        else weatherIcon.setImageResource(R.drawable.cloud);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRequirementsAndFetch();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }
}
