package com.dopamin.markod.objects;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dopamin.markod.R;
import com.dopamin.markod.activity.MainActivity;
import com.dopamin.markod.request.PlacesResult;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kadir on 19.10.2015.
 */
public class TokenManager {

    private Context context;
    public TokenResult delegateTokenResult = null;

    public TokenManager(Context context) {
        this.context = context;
    }

    private String authenticateURL = MainActivity.MDS_SERVER + "/mds/signup/authenticate";

    public String getToken(User user) {

        Gson gson = new Gson();
        user.setApi_key(MainActivity.MDS_API_KEY);

        Log.v(MainActivity.TAG, "query user: " + gson.toJson(user));

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, authenticateURL,
                gson.toJson(user), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("volley", "response: " + response);
                String status = null;

                try {
                    status = response.get("status").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (status != null) {
                    if (status.equalsIgnoreCase("OK")) {
                        try {
                            delegateTokenResult.tokenSuccess(response.get("token").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (status.equalsIgnoreCase("EXPIRED")) {
                        delegateTokenResult.tokenExpired();
                    }
                    else {
                        delegateTokenResult.tokenFailed();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(MainActivity.TAG, "Volley: User login error.");
                Log.e(MainActivity.TAG, error.toString());
            }
        });

        // Set timeout to 15 sec, and try only one time
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                1, //DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this.context).add(jsObjRequest);

        return null;
    }
}
