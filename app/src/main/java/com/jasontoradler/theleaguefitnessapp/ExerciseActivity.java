package com.jasontoradler.theleaguefitnessapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.jasontoradler.theleaguefitnessapp.data.Exercise;
import com.jasontoradler.theleaguefitnessapp.data.WorkoutStorage;

import java.util.List;

/**
 * Displays all exercise data and handles switching between exercises in a workout. Also, allows
 * editing default exercise parameters and saving them.
 */
public class ExerciseActivity extends Activity implements
        ExerciseProgressFragment.ButtonClickListener,
        ExerciseEditFragment.ButtonClickListener {

    public static final String EXTRA_RESTART_WORKOUT = "extraRestartWorkout";
    private static final String TAG = "ExerciseActivity";
    private List<Exercise> workout;
    private Exercise currentExercise;
    private int currentExerciseIndex;
    private TextView title;
    private ImageButton prevExerciseButton;
    private ImageButton nextExerciseButton;
    private NetworkImageView image;
    private ExerciseProgressFragment exerciseProgressFragment;
    private ExerciseEditFragment exerciseEditFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        title = (TextView) findViewById(R.id.exerciseTitle);
        prevExerciseButton = (ImageButton) findViewById(R.id.prevExerciseButton);
        nextExerciseButton = (ImageButton) findViewById(R.id.nextExerciseButton);
        image = (NetworkImageView) findViewById(R.id.exerciseImage);

        initWorkout();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        exerciseProgressFragment = ExerciseProgressFragment.newInstance(currentExercise);
        transaction.add(R.id.exerciseFragmentLayout, exerciseProgressFragment);
        transaction.commit();

        exerciseEditFragment = ExerciseEditFragment.newInstance(currentExercise);

        final Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.isEmpty()) {
            if (extras.containsKey(EXTRA_RESTART_WORKOUT)) {
                restartWorkout();
            }
        }

        displayCurrentExercise();
    }

    private void restartWorkout() {
        if (workout != null) {
            for (final Exercise exercise : workout) {
                exercise.resetProgress();
            }
        }
    }

    private void initWorkout() {
        workout = WorkoutStorage.instance().loadWorkout(this);

        currentExerciseIndex = 0;

        prevExerciseButton.setVisibility(View.INVISIBLE);

        // display the first exercise
        if (workout != null && !workout.isEmpty()) {
            nextExerciseButton.setVisibility(workout.size() > 1 ? View.VISIBLE : View.INVISIBLE);

            currentExercise = workout.get(currentExerciseIndex);
        } else {
            nextExerciseButton.setVisibility(View.INVISIBLE);
        }

        Log.d(TAG, "workout: " + workout);
    }

    private void resetCurrentExercise() {
        if (currentExercise != null) {
            currentExercise.resetProgress();
        }
    }

    private void displayCurrentExercise() {
        if (currentExercise != null) {
            final String[] description = currentExercise.description;
            if (description != null && description.length > 0) {
                title.setText(description[0]);
            }
            if (currentExercise.img != null && currentExercise.img.src != null) {
                // attempt to resize image based on height field returned from server, but make
                // sure image is not too large
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) image.getLayoutParams();
                final int height = currentExercise.img.height < 300 ? currentExercise.img.height : 300;
                params.height = (int) (getResources().getDisplayMetrics().density * height);
                image.setLayoutParams(params);
                image.setImageUrl(currentExercise.img.src, NetworkTool.instance(this).getImageLoader());
            }

            if (exerciseProgressFragment != null && exerciseProgressFragment.isAdded()) {
                exerciseProgressFragment.setCurrentExercise(currentExercise);
                exerciseProgressFragment.displayCurrentExercise();
            } else if (exerciseEditFragment != null && exerciseEditFragment.isAdded()) {
                exerciseEditFragment.setCurrentExercise(currentExercise);
                exerciseEditFragment.displayCurrentExercise();
            }
        }
    }

    /**
     * Display detailed instructions when button is clicked.
     *
     * @param view 'detailed instructions' button
     */
    public void onDetailedInstructionsClicked(View view) {
        if (currentExerciseIndex >= 0 && currentExerciseIndex < workout.size()) {
            final Exercise exercise = workout.get(currentExerciseIndex);
            if (exercise != null) {
                StringBuilder sb = new StringBuilder();
                // first string in description is the title so start at 1
                for (int ii = 1; ii < exercise.description.length; ++ii) {
                    sb.append(exercise.description[ii]);
                    sb.append('\n');
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(exercise.description[0]);
                builder.setMessage(sb.toString());
                builder.create();
                builder.show();
            }
        }
    }

    public void onPrevExerciseClick(View view) {
        if (currentExerciseIndex > 0) {
            --currentExerciseIndex;
            currentExercise = workout.get(currentExerciseIndex);

            if (currentExerciseIndex == 0) {
                prevExerciseButton.setVisibility(View.INVISIBLE);
            }
            nextExerciseButton.setVisibility(View.VISIBLE);

            displayCurrentExercise();
        }
    }

    public void onNextExerciseClick(View view) {
        if (currentExerciseIndex < (workout.size() - 1)) {
            ++currentExerciseIndex;
            currentExercise = workout.get(currentExerciseIndex);

            if (currentExerciseIndex == (workout.size() - 1)) {
                nextExerciseButton.setVisibility(View.INVISIBLE);
            }
            prevExerciseButton.setVisibility(View.VISIBLE);

            displayCurrentExercise();
        }
    }

    @Override
    public void onEditButtonClicked() {
        // replace progress fragment with edit fragment
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        currentExercise = workout.get(currentExerciseIndex);
        exerciseEditFragment.setCurrentExercise(currentExercise);
        Log.d(TAG, "onEditButtonClicked: " + currentExercise);
        transaction.replace(R.id.exerciseFragmentLayout, exerciseEditFragment);
        transaction.commit();
    }

    @Override
    public void onResetButtonClicked() {
        Log.d(TAG, "onResetButtonClicked");
        resetCurrentExercise();
        displayCurrentExercise();
    }

    @Override
    public void onSaveButtonClicked() {
        Log.d(TAG, "onSaveButtonClicked");
        final WorkoutStorage workoutStorage = WorkoutStorage.instance();
        workoutStorage.saveWorkout(this, workout, workoutStorage.getCurrentCode(this));
    }

    @Override
    public void onExerciseComplete() {
        Log.d(TAG, "onExerciseComplete");
        boolean allDone = true;
        for (final Exercise exercise : workout) {
            if (!exercise.isDone()) {
                allDone = false;
                break;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(
                allDone ? R.string.workout_complete_title : R.string.exercise_complete_title);
        builder.setMessage(
                allDone ? R.string.workout_complete_message : R.string.exercise_complete_message);
        builder.create();
        builder.show();
    }

    @Override
    public void onCancelButtonClicked() {
        Log.d(TAG, "onCancelButtonClicked");
        switchToProgress();
    }

    @Override
    public void onUpdateButtonClicked(Exercise exercise) {
        Log.d(TAG, "onUpdateButtonClicked: " + exercise);
        workout.set(currentExerciseIndex, exercise);
        currentExercise = exercise;
        final WorkoutStorage workoutStorage = WorkoutStorage.instance();
        workoutStorage.saveWorkout(this, workout, workoutStorage.getCurrentCode(this));
        switchToProgress();
    }

    private void switchToProgress() {
        // replace edit fragment with progress fragment
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        exerciseProgressFragment.setCurrentExercise(currentExercise);
        transaction.replace(R.id.exerciseFragmentLayout, exerciseProgressFragment);
        transaction.commit();
    }
}
