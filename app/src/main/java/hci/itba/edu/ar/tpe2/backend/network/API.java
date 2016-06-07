package hci.itba.edu.ar.tpe2.backend.network;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Properties;

import hci.itba.edu.ar.tpe2.backend.FileManager;
import hci.itba.edu.ar.tpe2.backend.data.City;
import hci.itba.edu.ar.tpe2.backend.data.Language;

/**
 * Singleton class used for making requests to the API.
 */
public class API {
    private static API instance = new API();

    private API() {}

    public enum Method {
        getcities, getlanguages, getairports, getflightstatus
    }

    public enum Service {
        misc, geo, booking, review, status
    }

    public static final String API_BASE_URL = "http://eiffel.itba.edu.ar/hci/service4/",
                                LOG_TAG = "VOLANDO";

    /**
     * @return The singleton instance.
     */
    public static API getInstance() {
        return instance;
    }

    //TODO use Flight object?
    public void getFlightStatus(String airlineId, int flightNum, final Context context, final NetworkRequestCallback<String> callback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getflightstatus.name());
        params.putString("airline_id", airlineId);
        params.putString("flight_number", Integer.toString(flightNum));
        new APIRequest(Service.status, params) {
            @Override
            protected void successCallback(String result) {
                throw new UnsupportedOperationException("Unfinished method.");
                //TODO extract meaninful information, pass it to an object and run the
                //callback with it

                int startIndex = result.indexOf("status") + 8,
                        endIndex = result.lastIndexOf(']') + 1;
                String data = result.substring(startIndex, endIndex);
                Gson g = new Gson();
                Language[] langs = g.fromJson(data, Language[].class);
                //TODO stop saving them here, save them in the callback if anything
                if(new FileManager(context).saveLanguages(langs)) {
                    if(callback != null) {
                        callback.execute(context, "");
                    }
                }
                else {
                    Log.w(LOG_TAG, "Couldn't save languages.");
                }
            }
        }.execute();
    }

    public void loadAllCities(final Context context, final NetworkRequestCallback<City[]> callback) {
        final Service service = Service.geo;
        final Bundle params = new Bundle();
        params.putString("method", Method.getcities.name());
        count(service, params, context, new NetworkRequestCallback<Integer>() {
            @Override
            public void execute(Context c, Integer cityCount) {
                //TODO handle null
                //Now that we have the total, fetch again with a page of size <cityCount>
                params.putString("page_size", Integer.toString(cityCount));
                new APIRequest(service, params) {
                    @Override
                    protected void successCallback(String result) {
                        int startIndex = result.indexOf("cities") + 8,   //Start at cities' [
                                endIndex = result.lastIndexOf(']') + 1;         //End at cities' ]
                        String data = result.substring(startIndex, endIndex);
                        Gson g = new Gson();
                        City[] cities = g.fromJson(data, City[].class);
                        //TODO stop saving them here, save them in the callback if anything
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
        });
    }

    public void getLanguages(final Context context, final NetworkRequestCallback<Language[]> callback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getlanguages.name());
        new APIRequest(Service.misc, params) {
            @Override
            protected void successCallback(String result) {
                int startIndex = result.indexOf("languages") + 11,
                        endIndex = result.lastIndexOf(']') + 1;
                String data = result.substring(startIndex, endIndex);
                Gson g = new Gson();
                Language[] langs = g.fromJson(data, Language[].class);
                //TODO stop saving them here, save them in the callback if anything
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

    /**
     * Gets the count of results returned by the specified query, or {@code null} if the query
     * doesn't specify a total, and passes it to the specified callback function.
     *
     * @param service The service the request is for.
     * @param requestParams Request parameters.
     * @param context Context to run the callback with.
     * @param callback Callback which will receive the total, or {@code null} if not specified in
     *                 the request's response.
     */
    public void count(Service service, Bundle requestParams, final Context context, final NetworkRequestCallback<Integer> callback) {
        new APIRequest(service, requestParams) {
            @Override
            protected void successCallback(String result) {
                if(callback != null) {
                    Gson g = new Gson();
                    JsonObject json = g.fromJson(result, JsonObject.class);
                    JsonElement e = json.get("total");
                    Integer total = null;
                    if(e != null) {
                        total = e.getAsInt();
                    }
                    callback.execute(context, total);
                }
            }
        }.execute();
    }
}