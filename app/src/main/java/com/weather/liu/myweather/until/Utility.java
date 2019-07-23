package com.weather.liu.myweather.until;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.weather.liu.myweather.db.City;
import com.weather.liu.myweather.db.County;
import com.weather.liu.myweather.db.Province;
import com.weather.liu.myweather.gson.Weather;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    /**
     *解析处理服务器返回的省级数据
     */

    public static boolean handleProvinceResponse(String response){

        if (!TextUtils.isEmpty ( response )){

            try{

                JSONArray allProvince = new JSONArray ( response );

                for(int i = 0; i<allProvince.length (); i++){
                    JSONObject provinceObject = allProvince.getJSONObject ( i );
                    Province province = new Province ();
                    province.setProvinceCode ( provinceObject.getInt ( "id" ) );
                    province.setProvinceName ( provinceObject.getString ( "name" ) );
                    province.save ();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace ();
            }
        }
        return false;
    }

    /**
     * 解析处理服务器返回的市级数据
     */

    public static boolean handleCityResponse(String response , int provinecId){

        if (!TextUtils.isEmpty ( response )){

            try{

                JSONArray allCities = new JSONArray(response);

                for(int i = 0; i<allCities.length (); i++){
                    JSONObject cityObject = allCities.getJSONObject ( i );
                    City city = new City ();
                    city.setCityCode ( cityObject.getInt ( "id" ) );
                    city.setCityName ( cityObject.getString ( "name" ) );
                    city.setProvinceId ( provinecId );
                    city.save ();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace ();
            }
        }
        return false;
    }

    /**
     * 解析处理服务器返回的县级数据
     */

    public static boolean handleCountyResponse(String response , int cityId){

        if (!TextUtils.isEmpty ( response )){

            try{

                JSONArray allCounty = new JSONArray ( response );

                for(int i = 0; i<allCounty.length (); i++){
                    JSONObject countyObject = allCounty.getJSONObject ( i );
                    County county = new County ();
                    //county.setId ( countyObject.getInt ( "id" ) );
                    county.setCountyName ( countyObject.getString ( "name" ) );
                    county.setWeatherId ( countyObject.getString ( "weather_id" ) );
                    county.setCityId ( cityId );
                    county.save ();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace ();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response){

        try {
            JSONObject jsonObject = new JSONObject ( response );
            JSONArray jsonArray = jsonObject.getJSONArray ( "HeWeather" );
            String weatherContent = jsonArray.getJSONObject ( 0 ).toString ();
            return new Gson ().fromJson ( weatherContent , Weather.class );
        }catch (Exception e ){
            e.printStackTrace ();
        }
        return null;
    }
}
