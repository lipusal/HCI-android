package hci.itba.edu.ar.tpe2.backend;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class API {
    //    private static API instance = new API();
    public enum Method { //No need for static, inner enums are implicitly static
        GETCITIES, GETAIRPORTS
    }

    public enum Service {
        misc, geo, booking, review
    }

    public static final String API_BASE_URL = "http://eiffel.itba.edu.ar/hci/service4/",
                                LOG_TAG = "VOLANDO";

    public static void loadCities(final Context context, final NetworkRequestCallback<City[]> callback) {
        Bundle params = new Bundle();
        params.putString("method", "getcities");
        new APIRequest(Service.geo, params) {
            @Override
            protected void successCallback(String result) {
                int startIndex = result.indexOf("cities") + 8,   //Start at cities' [
                    endIndex = result.lastIndexOf(']') + 1;         //End at cities' ]
                String data = result.substring(startIndex, endIndex);
                Gson g = new Gson();
                City[] cities = g.fromJson(data, City[].class);
                if(new FileManager(context).saveCities(cities)) {
                    if(callback != null) {
                        callback.execute(context, cities);
                    }
                }
                else {
                    Log.w(LOG_TAG, "Couldn't save cities.");
                }
            }
        }.execute();
    }

    public static void loadLanguages(final Context context, final NetworkRequestCallback<Language[]> callback) {
        Bundle params = new Bundle();
        params.putString("method", "getlanguages");
        new APIRequest(Service.misc, params) {
            @Override
            protected void successCallback(String result) {
                int startIndex = result.indexOf("languages") + 11,
                        endIndex = result.lastIndexOf(']') + 1;
                String data = result.substring(startIndex, endIndex);
                Gson g = new Gson();
                Language[] langs = g.fromJson(data, Language[].class);
                if(new FileManager(context).saveLanguages(langs)) {
                    if(callback != null) {
                        callback.execute(context, langs);
                    }
                }
                else {
                    Log.w(LOG_TAG, "Couldn't save languages.");
                }
            }
        }.execute();
    }
}