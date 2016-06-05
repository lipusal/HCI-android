package hci.itba.edu.ar.tpe2.backend;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Abstract class for asynchronously sending and interpreting API requests.
 */
public abstract class APIRequest {
    public static final String ERR_STRING = "Connection error";
    private Bundle params;
    private String service;

    public APIRequest(API.Service service, Bundle params) {
        this.params = params;
        this.service = service.name();
    }

    public boolean execute() {
        StringBuilder requestURL = new StringBuilder(API.API_BASE_URL + service + ".groovy?");
        for(String key : params.keySet()) {
            requestURL.append(key + "=" + params.getString(key) + "&");
        }
        requestURL.setLength(requestURL.length() - 1);    //Strip last &
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
                if(s.equals(ERR_STRING) || s.contains("\"error\":")) {
                    errorCallback(s);
                }
                else {
                    successCallback(s);
                }
            }
        }.execute(requestURL.toString());
        return false;
    }

    protected abstract void successCallback(String result);

    protected void errorCallback(String result) {
        Log.e(API.LOG_TAG, "Error: " + result);
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

    //TODO use POST http://stackoverflow.com/questions/4205980/java-sending-http-parameters-via-post-method-easily

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
