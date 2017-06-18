package hci.itba.edu.ar.tpe2.backend.network;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Class used for querying Flickr for images
 */
public abstract class FlickrRequest {
    public static final String ERR_STRING = "Connection error";
    private static final String API_KEY = "9a5f81e1e267f943ba8bbc71ae056840";
    private static final String FLICKR_API_BASE_URL = "https://api.flickr.com/services/rest/?method=flickr.photos.search&" +
            "tags=landscape&" +
            "media=photos&" +
            "extra=url_l&" +
            "format=json&" +
            "nojsoncallback=1&" +
            "api_key=" + API_KEY;

    private String query;

    public FlickrRequest(String imageQuerySearch) {
        this.query = imageQuerySearch;
    }

    public boolean execute() {
        String requestURL = null;
        try {
            requestURL = FLICKR_API_BASE_URL + "&text=" + URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    return performRequest(params[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                    return ERR_STRING;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                JsonObject flickrData = new Gson().fromJson(s, JsonObject.class);
                if (s.equals(ERR_STRING) || !flickrData.get("stat").getAsString().equals("ok")) {
                    errorCallback(s);
                } else {
                    successCallback(s);
                }
            }
        }.execute(requestURL.toString());
        return false;
    }

    protected abstract void successCallback(String result);

    protected void errorCallback(String result) {
        Log.e(API.LOG_TAG, "Flickr error: " + result);
    }

    private String performRequest(String request) throws IOException {
        URL url = new URL(request);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("charset", "utf-8");
        conn.setDoInput(true);
        conn.connect();
        return inputStreamToString(conn.getInputStream());
    }

    private String inputStreamToString(InputStream i) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(i, "UTF-8"));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            line = br.readLine();
        }
        return sb.toString();
    }
}
