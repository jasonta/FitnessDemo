package com.jasontoradler.theleaguefitnessapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasontoradler.theleaguefitnessapp.data.Exercise;

import java.io.IOException;
import java.util.List;

import static com.android.volley.Request.Method.POST;

/**
 * Provides a set of methods to retrieve exercise data from the internet, parse results,
 * and access them from the UI. Includes caching image data internally as well.
 */
public class NetworkTool {

    private static final int INITIAL_TIMEOUT_MS = 10000;
    private static final int MAX_NUM_RETRIES = 3;
    private static final int BACKOFF_MULTIPLIER = 2;
    private static final String TAG = "NetworkTool";
    private static final String BODY_CONTENT_TYPE = "application/json";
    private static final String URL = "https://arcane-anchorage-34204.herokuapp.com/handleCode";
    private static NetworkTool networkTool;
    private final RequestQueue requestQueue;
    private final ImageLoader imageLoader;

    private NetworkTool(final Context context) {
        requestQueue = Volley.newRequestQueue(context);

        imageLoader = new ImageLoader(
                requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(
                            (int) (Runtime.getRuntime().maxMemory() / 4)) {
                        @Override
                        protected int sizeOf(String key, Bitmap bitmap) {
                            return bitmap.getByteCount();
                        }
                    };

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    /**
     * Provide a singleton instance of the NetworkTool.
     *
     * @param context Context required to perform HTTP requests
     * @return singleton instance of this class
     */
    public static NetworkTool instance(final Context context) {
        if (networkTool == null) {
            networkTool = new NetworkTool(context);
        }
        return networkTool;
    }

    public void requestWorkout(
            Context context,
            final String code,
            final WorkoutRequestListener workoutRequestListener) {
        if (context != null && code != null && workoutRequestListener != null) {
            WorkoutRequest request = new WorkoutRequest(
                    code,
                    workoutRequestListener,
                    context.getResources());
            request.setRetryPolicy(new DefaultRetryPolicy(
                    INITIAL_TIMEOUT_MS,
                    MAX_NUM_RETRIES,
                    BACKOFF_MULTIPLIER));
            requestQueue.add(request);
        }
    }

    /**
     * Used to handle loading and displaying images as part of exercise routines.
     *
     * @return the ImageLoader used to cache and display images
     */
    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    /**
     * Methods used in @{@link NetworkTool#requestWorkout(Context context, String, WorkoutRequestListener)} to handle
     * network responses.
     */
    public interface WorkoutRequestListener {
        void onSuccess(List<Exercise> exercises);

        void onError(String errorMsg);
    }

    /**
     * Custom request class to handle creating a POST request with a workout code for the body, and
     * calling the appropriate listener method with the response data parsed from JSON to POJOs or
     * any network error.
     */
    private static final class WorkoutRequest extends StringRequest {

        private final String code;

        public WorkoutRequest(
                final String code,
                final WorkoutRequestListener workoutRequestListener,
                final Resources resources) {
            super(POST,
                    URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "onResponse: " + response);
                            final List<Exercise> exercises = parseWorkoutResponse(response);
                            if (workoutRequestListener != null) {
                                if (exercises != null && !exercises.isEmpty()) {
                                    workoutRequestListener.onSuccess(exercises);
                                } else {
                                    workoutRequestListener.onError(resources.getString(R.string.noWorkoutFoundError));
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "onError: " + error);
                            if (workoutRequestListener != null) {
                                if (error instanceof TimeoutError) {
                                    workoutRequestListener.onError(resources.getString(R.string.pleaseTryAgainError));
                                } else {
                                    workoutRequestListener.onError(resources.getString(R.string.noWorkoutFoundError));
                                }
                            }
                        }
                    });

            this.code = code;
        }

        private static List<Exercise> parseWorkoutResponse(final String response) {
            ObjectMapper mapper = new ObjectMapper();
            List<Exercise> exercises = null;
            try {
                exercises = mapper.readValue(response, new TypeReference<List<Exercise>>() {
                });
                Log.d(TAG, "exercises: " + exercises);
            } catch (IOException e) {
                Log.e(TAG, "error parsing response: " + e);
                e.printStackTrace();
            }
            return exercises;
        }

        /**
         * Override request method to return POST body ("code")
         */
        @Override
        public byte[] getBody() throws AuthFailureError {
            final String jsonCode = "{\"code\":\"" + code + "\"}";
            Log.d(TAG, "getBody: " + jsonCode);
            return jsonCode.getBytes();
        }

        @Override
        public String getBodyContentType() {
            Log.d(TAG, "getBodyContentType: " + BODY_CONTENT_TYPE);
            return BODY_CONTENT_TYPE;
        }
    }
}
