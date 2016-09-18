package com.github.charmasaur.romperstomper;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sender {
  private static final String TAG = Sender.class.getSimpleName();
  private static final String URL = "http://romper-stomper.appspot.com/here";

  public interface Callback {
    void onResults(List<String> data);
    void onStatus(String status);
  }

  private final Callback callback;
  private final RequestQueue queue;
  private final String token;

  public Sender(Context context, Callback callback) {
    this.callback = callback;

    queue = Volley.newRequestQueue(context);
    byte[] randomBytes = new byte[10];
    new SecureRandom().nextBytes(randomBytes);
    token = Base64.encodeToString(randomBytes, Base64.URL_SAFE);
    Log.i(TAG, "Made token: " + token);
    // TODO: Save to shared prefs so we have a persistent token
  }

  public void send(double lat, double lng, double acc, long time) {
    sendIt(lat, lng, acc, time, false);
  }

  public void update() {
    sendIt(0., 0., 0., 0, true);
  }

  private void sendIt(double lat, double lng, double acc, long time, boolean just_request) {
    // Request a string response from the provided URL.
    String url = URL + "?" +
        (just_request
            ? ""
            : ("lat=" + lat + "&lng=" + lng + "&acc=" + acc + "&tim=" + time))
        + "&token=" + token;
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.i(TAG, "Got response: " + response);
            parseResponse(response);
            callback.onStatus("Last query successful");
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.i(TAG, "Got error: " + error);
            callback.onStatus("Last query failed: " + error);
          }
        });
    // Add the request to the RequestQueue.
    queue.add(stringRequest);
  }

  private void parseResponse(String r) {
    callback.onResults(Arrays.asList(r.split("\\|")));
  }
}