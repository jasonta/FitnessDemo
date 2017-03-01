package com.jasontoradler.theleaguefitnessapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jasontoradler.theleaguefitnessapp.data.Exercise;
import com.jasontoradler.theleaguefitnessapp.data.WorkoutStorage;

import java.util.List;

/**
 * Home screen, which provides UI to request a new workout or resume/restart saved workout.
 */
public class MainActivity extends Activity {

    private Button newWorkoutButton;
    private Button resumeWorkoutButton;
    private Button restartWorkoutButton;
    private Button clearButton;
    private AutoCompleteTextView newWorkoutCode;
    private TextView resumeWorkoutCode;
    private ProgressBar loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newWorkoutButton = (Button) findViewById(R.id.newWorkoutButton);
        resumeWorkoutButton = (Button) findViewById(R.id.resumeWorkoutButton);
        resumeWorkoutButton.setTransformationMethod(null);
        restartWorkoutButton = (Button) findViewById(R.id.restartWorkoutButton);
        restartWorkoutButton.setTransformationMethod(null);
        clearButton = (Button) findViewById(R.id.clearButton);
        clearButton.setTransformationMethod(null);

        newWorkoutCode = (AutoCompleteTextView) findViewById(R.id.newWorkoutCode);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.defaultCodes));
        newWorkoutCode.setAdapter(adapter);
        newWorkoutCode.setThreshold(1);
        newWorkoutCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (newWorkoutCode.getText().toString().isEmpty()) {
                    newWorkoutButton.setEnabled(false);
                    clearButton.setEnabled(false);
                } else {
                    newWorkoutButton.setEnabled(true);
                    clearButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        resumeWorkoutCode = (TextView) findViewById(R.id.resumeWorkoutCode);

        loadingProgress = (ProgressBar) findViewById(R.id.loadingProgress);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final WorkoutStorage workoutStorage = WorkoutStorage.instance();
                final boolean isWorkoutStored = workoutStorage.isWorkoutStored(MainActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isWorkoutStored) {
                            resumeWorkoutButton.setVisibility(View.VISIBLE);
                            resumeWorkoutButton.setEnabled(true);
                            resumeWorkoutCode.setText(workoutStorage.getCurrentCode(MainActivity.this));
                            resumeWorkoutCode.setVisibility(View.VISIBLE);
                            restartWorkoutButton.setVisibility(View.VISIBLE);
                            restartWorkoutButton.setEnabled(true);
                        } else {
                            resumeWorkoutButton.setVisibility(View.GONE);
                            resumeWorkoutCode.setVisibility(View.GONE);
                            restartWorkoutButton.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }).start();
    }

    public void onClear(View view) {
        clearSelection();
    }

    public void onResumeWorkoutClicked(View view) {
        Intent intent = new Intent(MainActivity.this, ExerciseActivity.class);
        startActivity(intent);
    }

    public void onRestartWorkoutClicked(View view) {
        Intent intent = new Intent(MainActivity.this, ExerciseActivity.class);
        intent.putExtra(ExerciseActivity.EXTRA_RESTART_WORKOUT, true);
        startActivity(intent);
    }

    public void onNewWorkoutButtonClicked(View view) {
        toggleInput(false);
        submitCode(newWorkoutCode.getText().toString());
    }

    private void toggleInput(boolean isEnabled) {
        loadingProgress.setVisibility(isEnabled ? View.INVISIBLE : View.VISIBLE);
        clearButton.setEnabled(isEnabled);
        newWorkoutCode.setEnabled(isEnabled);
        if (!TextUtils.isEmpty(newWorkoutCode.getText().toString())) {
            newWorkoutButton.setEnabled(isEnabled);
        }
        if (!TextUtils.isEmpty(resumeWorkoutCode.getText().toString())) {
            resumeWorkoutButton.setEnabled(isEnabled);
        }
        resumeWorkoutButton.setEnabled(isEnabled);
        restartWorkoutButton.setEnabled(isEnabled);
    }

    private void submitCode(final String code) {
        if (!TextUtils.isEmpty(code)) {
            NetworkTool.instance(this).requestWorkout(this, code, new NetworkTool.WorkoutRequestListener() {
                @Override
                public void onSuccess(List<Exercise> workout) {
                    if (workout != null && !workout.isEmpty()) {
                        // initialize the progress of each exercise since this is a new workout
                        for (final Exercise exercise : workout) {
                            exercise.initProgress();
                        }

                        WorkoutStorage.instance().saveWorkout(MainActivity.this, workout, code);

                        Intent intent = new Intent(MainActivity.this, ExerciseActivity.class);
                        startActivity(intent);
                    } else {
                        onError(getResources().getString(R.string.noWorkoutFoundError));
                    }

                    toggleInput(true);
                }

                @Override
                public void onError(String errorMsg) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(true)
                            .setTitle("Oops! Something went wrong...")
                            .setMessage(errorMsg)
                            .create()
                            .show();

                    toggleInput(true);
                }
            });
        }
    }

    private void clearSelection() {
        newWorkoutCode.setText("");
    }
}
