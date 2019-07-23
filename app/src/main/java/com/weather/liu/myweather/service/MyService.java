package com.weather.liu.myweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import com.weather.liu.myweather.gson.Weather;
import com.weather.liu.myweather.until.HttpUntil;
import com.weather.liu.myweather.until.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent , int flags , int startId){

        updateWeather();
        AlarmManager manager = (AlarmManager)getSystemService ( ALARM_SERVICE );
        int anHour = 8*60*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime () + anHour;
        Intent i = new Intent ( this , MyService.class );
        PendingIntent pendingIntent = PendingIntent.getService ( this , 0 , i , 0 );
        manager.cancel ( pendingIntent );
        manager.set ( AlarmManager.ELAPSED_REALTIME_WAKEUP , triggerAtTime , pendingIntent );
        return super.onStartCommand ( intent , flags , startId );
    }

    private void updateWeather() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences ( this );
        String weatherString = prefs.getString ( "weather" , null );
        if (weatherString != null){

            Weather weather = Utility.handleWeatherResponse ( weatherString );
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=3012f1744a724596a4a0c60589385ff9";
            HttpUntil.sendHttpRequest ( weatherUrl, new Callback () {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace ();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseText = response.body ().string ();
                    Weather weather = Utility.handleWeatherResponse ( responseText );
                    if (weather != null && "ok".equals ( weather.status )){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences( MyService.this ).edit ();
                        editor.putString ( "weather" , responseText );
                        editor.apply ();
                    }
                }
            } );
        }
    }
}
