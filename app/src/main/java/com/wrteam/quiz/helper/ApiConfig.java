package com.wrteam.quiz.helper;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.wrteam.quiz.Constant;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ApiConfig {

    public static String VolleyErrorMessage(VolleyError error) {
        String message = "";
        try {
            if (error instanceof NetworkError) {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (error instanceof ServerError) {
                message = "The server could not be found. Please try again after some time!!";
            } else if (error instanceof AuthFailureError) {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again after some time!!";
            } else if (error instanceof TimeoutError) {
                message = "Connection TimeOut! Please check your internet connection.";
            } else
                message = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    public void testMethod() {
        Map<String, String> params = new HashMap<>();

        ApiConfig.RequestToVolley(new ApiConfig.VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {

                }
            }
        }, params);
    }

    public static void MultipartRequestToVolley(final VolleyCallback callback, final Map<String, String> params, final Map<String, String> fileParams) {
        VolleyMultiPartRequest multipartRequest = new VolleyMultiPartRequest(Constant.QUIZ_URL, response -> callback.onSuccess(true, response),
                error -> callback.onSuccess(false, "")) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params1 = new HashMap<String, String>();
                params1.put(Constant.AUTHORIZATION, "Bearer " + AppController.createJWT("quiz", "quiz Authentication"));
                return params1;
            }
            @Override
            public Map<String, String> getDefaultParams() {
                params.put(Constant.accessKey, Constant.accessKeyValue);
                return params;
            }
            @Override
            public Map<String, String> getFileParams() {
                return fileParams;
            }
        };
        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(multipartRequest);
    }

    public static void RequestToVolley(final VolleyCallback callback, final Map<String, String> params) {


        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.QUIZ_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess(true, response);
            }

        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        callback.onSuccess(false, "");


                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params1 = new HashMap<String, String>();
                params1.put(Constant.AUTHORIZATION, "Bearer " + AppController.createJWT("quiz", "quiz Authentication"));
                return params1;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                params.put(Constant.accessKey, Constant.accessKeyValue);
                return params;
            }
        };
        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(stringRequest);


    }


    public interface VolleyCallback {
        void onSuccess(boolean result, String message);
        //void onSuccessWithMsg(boolean result, String message);

    }


    public static boolean CheckValidation(String item, boolean emailValidation, boolean mobileValidation) {
        if (item.length() == 0)
            return true;
        else if (emailValidation && (!android.util.Patterns.EMAIL_ADDRESS.matcher(item).matches()))
            return true;
        else return mobileValidation && (item.length() < 9 || item.length() > 11);
    }

}
