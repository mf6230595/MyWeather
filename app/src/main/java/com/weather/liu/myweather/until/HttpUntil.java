package com.weather.liu.myweather.until;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUntil {

    public static void sendHttpRequest(String address , okhttp3.Callback callback){

        OkHttpClient client = new OkHttpClient (  );

        Request request = new Request.Builder (  )
                .url ( address )
                .build ();
        client.newCall ( request ).enqueue ( callback );            //enqueue????
    }
}
