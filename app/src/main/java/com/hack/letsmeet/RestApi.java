package com.hack.letsmeet;

import android.content.Context;
import android.util.Log;

import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by bilal on 2014-09-20.
 */
public class RestApi {
    private static RestApi instance = null;
    private static final String BASE_URL="http://htn-letsmeet2.cloudapp.net:8080/";

    public enum Method {
        GET, POST
    };

    private RequestQueue queue;

    private String userid = "";
    private String accessToken = "";

    public static RestApi getInstance() {
        if (instance == null) {
            instance = new RestApi();
        }

        return instance;
    }

    private RestApi() {

    }
    public void request(Context context, String endpoint, Method method, JSONObject params, Response.Listener responseHandler) {
        if (userid.length() > 0 && accessToken.length() > 0) {

            request(context, endpoint, method, params, responseHandler, true);
        } else {
            request(context, endpoint, method, params, responseHandler, false);
        }
    }

    public void request(Context context, String endpoint, Method method, JSONObject params, Response.Listener responseHandler, final Boolean authenticated) {
        RequestQueue queue = Volley.newRequestQueue(context);
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("RestApi", new String(volleyError.networkResponse.data));
            }
        };

        JsonObjectRequest request = null;

        if (method == Method.GET) {
            request = new JsonObjectRequest(Request.Method.GET,
                    BASE_URL + endpoint,
                    params,
                    responseHandler,
                    errorListener) {
                @Override
                public HashMap<String, String> getHeaders() {
                    HashMap<String, String> params = new HashMap<String, String>();
                    if (authenticated) {
                        params.put("X-WWW-Authenticate", userid + " " + accessToken);
                    }

                    return params;
                }
            };
        } else if (method == Method.POST) {
            request = new JsonObjectRequest(Request.Method.POST,
                    BASE_URL+endpoint,
                    params,
                    responseHandler,
                    errorListener){
                @Override
                public HashMap<String, String> getHeaders() {
                    HashMap<String, String> params = new HashMap<String, String>();
                    if (authenticated) {
                        params.put("X-WWW-Authenticate", userid + " " + accessToken);
                    }

                    return params;
                }
            };
        }

        queue.add(request);

    }

    public void setAuth(String userId, String accessToken) {
        this.userid = userId;
        this.accessToken = accessToken;

    }
}
