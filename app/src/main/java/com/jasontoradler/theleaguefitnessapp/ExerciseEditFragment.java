package com.jasontoradler.theleaguefitnessapp;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jasontoradler.theleaguefitnessapp.data.Exercise;

/**
 * Display and allow the user to edit the current exercise. The {@link ExerciseActivity} must
 * implement the {@link ButtonClickListener} interface to allow handling cancel and update button
 * click events.
 * <p>
 * Use the {@link ExerciseEditFragment#newInstance} factory method to create an instance of this
 * fragment with the current exercise.
 */
public class ExerciseEditFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ExerciseEditFragment";

    private ViewGroup performLayout;
    private ViewGroup completeLayout;
    private ViewGroup repeatLayout;
    private ViewGroup holdLayout;
    private TextView performValueText;
    private TextView performText1;
    private TextView performText2;
    private TextView completeValueText;
    private TextView completeText1;
    private TextView completeText2;
    private TextView repeatValueText;
    private TextView repeatText1;
    private TextView repeatText2;
    private TextView holdValueText;
    private TextView holdText1;
    private TextView holdText2;
    private ImageButton performDecrementButton;
    private ImageButton performIncrementButton;
    private ImageButton completeDecrementButton;
    private ImageButton completeIncrementButton;
    private ImageButton repeatDecrementButton;
    private ImageButton repeatIncrementButton;
    private ImageButton holdDecrementButton;
    private ImageButton holdIncrementButton;
    private Button cancelButton;
    private Button updateButton;
    private Exercise currentExercise;
    private ButtonClickListener buttonClickListener;

    public ExerciseEditFragment() {
        // Required empty public constructor
    }

    /**
     * Create a new fragment with the current exercise.
     *
     * @param exercise current exercise to display
     * @return A new instance of the fragment
     */
    public static ExerciseEditFragment newInstance(Exercise exercise) {
        ExerciseEditFragment fragment = new ExerciseEditFragment();
        fragment.setCurrentExercise(exercise);
        return fragment;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exercise_edit, container, false);

        performLayout = (ViewGroup) view.findViewById(R.id.performEditLayout);
        performText1 = (TextView) performLayout.findViewById(R.id.editExerciseText1);
        performText2 = (TextView) performLayout.findViewById(R.id.editExerciseText2);
        performValueText = (TextView) performLayout.findViewById(R.id.editExerciseValue);
        performDecrementButton = (ImageButton) performLayout.findViewById(R.id.decrementButton);
        performDecrementButton.setOnClickListener(this);
        performIncrementButton = (ImageButton) performLayout.findViewById(R.id.incrementButton);
        performIncrementButton.setOnClickListener(this);

        completeLayout = (ViewGroup) view.findViewById(R.id.completeEditLayout);
        completeText1 = (TextView) completeLayout.findViewById(R.id.editExerciseText1);
        completeText2 = (TextView) completeLayout.findViewById(R.id.editExerciseText2);
        completeValueText = (TextView) completeLayout.findViewById(R.id.editExerciseValue);
        completeDecrementButton = (ImageButton) completeLayout.findViewById(R.id.decrementButton);
        completeDecrementButton.setOnClickListener(this);
        completeIncrementButton = (ImageButton) completeLayout.findViewById(R.id.incrementButton);
        completeIncrementButton.setOnClickListener(this);

        repeatLayout = (ViewGroup) view.findViewById(R.id.repeatEditLayout);
        repeatText1 = (TextView) repeatLayout.findViewById(R.id.editExerciseText1);
        repeatText2 = (TextView) repeatLayout.findViewById(R.id.editExerciseText2);
        repeatValueText = (TextView) repeatLayout.findViewById(R.id.editExerciseValue);
        repeatDecrementButton = (ImageButton) repeatLayout.findViewById(R.id.decrementButton);
        repeatDecrementButton.setOnClickListener(this);
        repeatIncrementButton = (ImageButton) repeatLayout.findViewById(R.id.incrementButton);
        repeatIncrementButton.setOnClickListener(this);

        holdLayout = (ViewGroup) view.findViewById(R.id.holdEditLayout);
        holdText1 = (TextView) holdLayout.findViewById(R.id.editExerciseText1);
        holdText2 = (TextView) holdLayout.findViewById(R.id.editExerciseText2);
        holdValueText = (TextView) holdLayout.findViewById(R.id.editExerciseValue);
        holdDecrementButton = (ImageButton) holdLayout.findViewById(R.id.decrementButton);
        holdDecrementButton.setOnClickListener(this);
        holdIncrementButton = (ImageButton) holdLayout.findViewById(R.id.incrementButton);
        holdIncrementButton.setOnClickListener(this);

        cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);
        updateButton = (Button) view.findViewById(R.id.updateButton);
        updateButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ButtonClickListener) {
            buttonClickListener = (ButtonClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ButtonClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        buttonClickListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayCurrentExercise();
    }

    public void setCurrentExercise(final Exercise exercise) {
        // make a copy so that if user cancels it will return to original version
        currentExercise = Exercise.copy(exercise);
        Log.d(TAG, "setCurrentExercise: " + currentExercise);
    }

    public void displayCurrentExercise() {
        Log.d(TAG, "displayCurrentExercise: " + currentExercise);
        if (currentExercise != null) {
            final Exercise.Instructions instructions = currentExercise.instructions;
            final Exercise.Progress progress = currentExercise.progress;
            String[] parts;
            parts = instructions.perform != null ? instructions.perform.split("\\d+") : null;
            if (parts != null && parts.length > 1) {
                performLayout.setVisibility(View.VISIBLE);
                performText1.setText(R.string.perform);
                performValueText.setText(String.valueOf(progress.performMax));
                performText2.setText(parts[1]);
            } else {
                performLayout.setVisibility(View.GONE);
            }

            parts = instructions.complete != null ? instructions.complete.split("\\d+") : null;
            if (parts != null && parts.length > 1) {
                completeLayout.setVisibility(View.VISIBLE);
                completeText1.setText(R.string.complete);
                completeValueText.setText(String.valueOf(progress.completeMax));
                completeText2.setText(parts[1]);
            } else {
                completeLayout.setVisibility(View.GONE);
            }

            parts = instructions.repeat != null ? instructions.repeat.split("\\d+") : null;
            if (parts != null && parts.length > 1) {
                repeatLayout.setVisibility(View.VISIBLE);
                repeatText1.setText(R.string.repeat);
                repeatValueText.setText(String.valueOf(progress.repeatMax));
                repeatText2.setText(parts[1]);
            } else {
                repeatLayout.setVisibility(View.GONE);
            }

            parts = instructions.hold != null ? instructions.hold.split("\\d+") : null;
            if (parts != null && parts.length > 1) {
                holdLayout.setVisibility(View.VISIBLE);
                holdText1.setText(R.string.hold);
                holdValueText.setText(String.valueOf(progress.holdMax));
                holdText2.setText(parts[1]);
            } else {
                holdLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.equals(cancelButton)) {
            buttonClickListener.onCancelButtonClicked();
        } else if (view.equals(updateButton)) {
            currentExercise.resetProgress();
            buttonClickListener.onUpdateButtonClicked(currentExercise);
        } else if (currentExercise != null && currentExercise.progress != null) {
            final Exercise.Progress progress = currentExercise.progress;
            if (view.equals(performDecrementButton) && progress.performMax > 0) {
                --progress.performMax;
            } else if (view.equals(performIncrementButton)) {
                ++progress.performMax;
            } else if (view.equals(completeDecrementButton) && progress.completeMax > 0) {
                --progress.completeMax;
            } else if (view.equals(completeIncrementButton)) {
                ++progress.completeMax;
            } else if (view.equals(repeatDecrementButton) && progress.repeatMax > 0) {
                --progress.repeatMax;
            } else if (view.equals(repeatIncrementButton)) {
                ++progress.repeatMax;
            } else if (view.equals(holdDecrementButton) && progress.holdMax > 0) {
                --progress.holdMax;
            } else if (view.equals(holdIncrementButton)) {
                ++progress.holdMax;
            }

            displayCurrentExercise();
        }
    }

    /**
     * Listener for the activity to handle the "Cancel" and "Update" button clicks.
     */
    public interface ButtonClickListener {
        void onCancelButtonClicked();

        void onUpdateButtonClicked(Exercise exercise);
    }
}
