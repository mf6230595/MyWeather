package com.weather.liu.myweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.weather.liu.myweather.gson.Forecast;
import com.weather.liu.myweather.gson.Weather;
import com.weather.liu.myweather.service.MyService;
import com.weather.liu.myweather.until.HttpUntil;
import com.weather.liu.myweather.until.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.IOException;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;

    private Button navButton;

    public SwipeRefreshLayout swipeRefresh;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forcecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_weather );

        weatherLayout = (ScrollView)findViewById ( R.id.weather_layout );
        titleCity = (TextView)findViewById ( R.id.title_city );
        titleUpdateTime = (TextView)findViewById ( R.id.title_update_time );
        degreeText = (TextView)findViewById ( R.id.degree_text );
        weatherInfoText = (TextView)findViewById ( R.id.weather_info_text );
        forcecastLayout = (LinearLayout)findViewById ( R.id.forecast_layout );
        aqiText = (TextView)findViewById ( R.id.aqi_text );
        pm25Text = (TextView)findViewById ( R.id.pm25_text );
        comfortText = (TextView)findViewById ( R.id.comfort_text );
        carWashText = (TextView)findViewById ( R.id.car_wash_text );
        sportText = (TextView)findViewById ( R.id.sport_text );
        drawerLayout = (DrawerLayout)findViewById ( R.id.drawer_layout );
        navButton = (Button)findViewById ( R.id.nav_button );

        swipeRefresh = (SwipeRefreshLayout)findViewById ( R.id.swipe_refresh );
        swipeRefresh.setColorSchemeResources ( R.color.colorPrimary );

        navButton.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer ( GravityCompat.START );
            }
        } );

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences ( this );

        String weatherString = prefs.getString ( "weather" , null );

        final String weatherId;

        if (weatherString != null){
            Weather weather = Utility.handleWeatherResponse ( weatherString );
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }
        else {
            weatherId = getIntent ().getStringExtra ("weather_id");
            weatherLayout.setVisibility ( View.INVISIBLE );
            requestWeather(weatherId);
        }
        swipeRefresh.setOnRefreshListener ( new SwipeRefreshLayout.OnRefreshListener () {
            @Override
            public void onRefresh() {
                requestWeather ( weatherId );
            }
        } );
    }

    public void requestWeather(String weatherId) {

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=3012f1744a724596a4a0c60589385ff9";
        HttpUntil.sendHttpRequest ( weatherUrl, new Callback () {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace ();
                runOnUiThread ( new Runnable () {
                    @Override
                    public void run() {
                        Toast.makeText ( WeatherActivity.this , "error" , Toast.LENGTH_SHORT ).show ();
                        swipeRefresh.setRefreshing ( false );
                    }
                } );

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body ().string ();
                final Weather weather = Utility.handleWeatherResponse ( responseText );
                runOnUiThread ( new Runnable () {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals ( weather.status )){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences ( WeatherActivity.this ).edit ();
                            editor.putString ( "weather" , responseText );
                            editor.apply ();
                            showWeatherInfo ( weather );
                        }
                        else {
                            Toast.makeText ( WeatherActivity.this , "error" , Toast.LENGTH_SHORT ).show ();
                        }
                        swipeRefresh.setRefreshing ( false );
                    }
                } );

            }
        } );
    }

    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;

        String updateTime = weather.basic.update.updateTime.split ( " " )[1];

        String degree = weather.now.temperature + "˚C";

        String weatherInfo = weather.now.more.info;

        titleCity.setText ( cityName );

        titleUpdateTime.setText ( updateTime );

        degreeText.setText ( degree );

        weatherInfoText.setText ( weatherInfo );

        forcecastLayout.removeAllViews ();

        for (Forecast forecast : weather.forecastList){

            View view = LayoutInflater.from ( this ).inflate ( R.layout.forecast_item , forcecastLayout , false );
            TextView dateText = (TextView)view.findViewById ( R.id.data_text );
            TextView infoText = (TextView)view.findViewById ( R.id.info_text );
            TextView maxText = (TextView)view.findViewById ( R.id.max_text );
            TextView minText = (TextView)view.findViewById ( R.id.min_text );
            dateText.setText ( forecast.date );
            infoText.setText ( forecast.more.info );
            maxText.setText ( forecast.temperature.max );
            minText.setText ( forecast.temperature.min );
            forcecastLayout.addView ( view );
        }
        if (weather.aqi != null){
            aqiText.setText ( weather.aqi.city.aqi );
            pm25Text.setText ( weather.aqi.city.pm25 );
        }
        String comfort = "舒适度" + weather.suggestion.comfort.info;
        String carWash = "洗车指数" + weather.suggestion.carWash.info;
        String sport = "运动指数" + weather.suggestion.sport.info;
        comfortText.setText ( comfort );
        carWashText.setText ( carWash );
        sportText.setText ( sport );
        weatherLayout.setVisibility ( View.VISIBLE );
        Intent intent = new Intent ( this , MyService.class );
        startService ( intent );
    }
}
