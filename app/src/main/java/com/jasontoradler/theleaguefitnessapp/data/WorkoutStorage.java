package com.jasontoradler.theleaguefitnessapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles storing exercises retrieved from the network as well as those locally modified.
 */
public final class WorkoutStorage {

    private static final String TAG = "WorkoutStorage";
    private static final String WORKOUT_FILE = "workout.dat";
    private static final String CODE = "workout_code";
    private static WorkoutStorage instance;

    private WorkoutStorage() {
    }

    public static WorkoutStorage instance() {
        if (instance == null) {
            instance = new WorkoutStorage();
        }
        return instance;
    }

    public String getCurrentCode(final Context context) {
        SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return prefs.getString(CODE, null);
    }

    public List<Exercise> loadWorkout(final Context context) {
        List<Exercise> list = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            list = mapper.readValue(new File(context.getFilesDir(), WORKOUT_FILE),
                    new TypeReference<List<Exercise>>() {});
        } catch (IOException e) {
            Log.e(TAG, "error reading from current workout file: " + e);
        }
        return list;
    }

    public boolean isWorkoutStored(final Context context) {
        final File workoutFile = new File(context.getFilesDir(), WORKOUT_FILE);
        return workoutFile.exists();
    }

    public void saveWorkout(final Context context, final List<Exercise> workout, final String code) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(context.getFilesDir(), WORKOUT_FILE), workout);

            SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            prefs.edit().putString(CODE, code).apply();
        } catch (IOException e) {
            Log.e(TAG, "error writing current workout to file: " + e);
        }
    }
}
