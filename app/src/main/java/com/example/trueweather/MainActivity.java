package com.example.trueweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private RelativeLayout mainRl;
    private ProgressBar loadPb;
    private TextView cityName,temperature,condition;
    private TextInputEditText cityEdit;
    private ImageView backIv,iconIv,searchIv;
    private RecyclerView weatherRv;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int permission=1;
    private String cName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        mainRl=findViewById(R.id.rlhome);
        loadPb=findViewById(R.id.pbloader);
        cityName=findViewById(R.id.ctname);
        temperature=findViewById(R.id.ttemp);
        condition=findViewById(R.id.conditon);
        cityEdit=findViewById(R.id.editct);
        backIv=findViewById(R.id.bgblack);
        iconIv=findViewById(R.id.launch);
        searchIv=findViewById(R.id.srch);
        weatherRv=findViewById(R.id.rv);
        weatherRVModelArrayList=new ArrayList<>();
        weatherRVAdapter=new WeatherRVAdapter(this,weatherRVModelArrayList);
        weatherRv.setAdapter(weatherRVAdapter);
        locationManager=(android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},permission);
        }
        try {


            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                cName = getCityName(location.getLongitude(), location.getLatitude());
                getWeatherInfo(cName);
            } else {
                cName = "Gurgaon";
                getWeatherInfo(cName);
            }
        }catch (SecurityException e){
            e.printStackTrace();
        }
        searchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city= Objects.requireNonNull(cityEdit.getText()).toString();
                if(city.isEmpty())
                {
                    Toast.makeText(MainActivity.this,"Please Enter the City",Toast.LENGTH_SHORT).show();
                }
                else {
                    cityName.setText(city);
                    getWeatherInfo(city);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==permission)
        {
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(MainActivity.this,"Permission Granted",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this,"Please provide the Permissions",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getCityName(double longitude, double latitude)
    {
        String cityname="Not Found";
        Geocoder gcd=new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);

            if (addresses != null) {
                for(Address adr:addresses)
                {
                    if(adr!=null)
                    {
                        String city= adr.getLocality();
                        if(city!=null && city.equals("")){
                            cityname=city;
                        }
                        else {
                            Log.d("TAG","City Not Found");
                            Toast.makeText(this,"User City Not Found..",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return cityname;
    }
    private void getWeatherInfo(String cityname)
    {
        String url="https://api.weatherapi.com/v1/forecast.json?key=yourkeyhere&q="+cityname+"&days=2&aqi=no&alerts=no";
        cityName.setText(cityname);
        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadPb.setVisibility(View.GONE);
                mainRl.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try {
                    String temp=response.getJSONObject("current").getString("temp_c");
                    temperature.setText(temp+"°c");
                    int isDay=response.getJSONObject("current").getInt("is_day");
                    String cond=response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String icon=response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("https:"+icon).into(iconIv);
                    condition.setText(cond);

                    if(isDay==1)
                    {
                        Picasso.get().load("https://images.unsplash.com/photo-1419833173245-f59e1b93f9ee?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D").into(backIv);

                    }
                    else {
                        Picasso.get().load("https://images.unsplash.com/photo-1619665481428-101a5daa4d15?q=80&w=1888&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D").into(backIv);
                    }

                    JSONObject forecastObj=response.getJSONObject("forecast");
                    JSONObject forecast0=forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray=forecast0.getJSONArray("hour");

                    String dateTimeString=response.getJSONObject("location").getString("localtime");
                    int hour = 0;

                    try {
                        SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date date = sdfInput.parse(dateTimeString);

                        SimpleDateFormat sdfOutput = new SimpleDateFormat("HH");
                        String hourString = sdfOutput.format(date);

                        hour = Integer.parseInt(hourString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
//                    Calendar calendar = Calendar.getInstance();
//                    int hour = calendar.get(Calendar.HOUR_OF_DAY);

                    for(int i=hour;i<hourArray.length();i++)
                    {
                        JSONObject hourObj=hourArray.getJSONObject(i);
                        String time=hourObj.getString("time");
                        String temper=hourObj.getString("temp_c");
                        String img=hourObj.getJSONObject("condition").getString("icon");
                        String wind=hourObj.getString("wind_kph");
                        weatherRVModelArrayList.add(new WeatherRVModel(time,temper,wind,img));

                    }

                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please Enter Valid City",Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);



    }
}