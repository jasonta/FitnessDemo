package com.jasontoradler.theleaguefitnessapp;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jasontoradler.theleaguefitnessapp.data.Exercise;

/**
 * Display the progress of the current exercise and allow user to change it. The
 * {@link ExerciseActivity} activity must implement the {@link ButtonClickListener} interface
 * to handle edit, reset, and save button clicks, as well as exercise complete events.
 * <p>
 * Use the {@link ExerciseProgressFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExerciseProgressFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ExerciseProgressFrag";

    private ViewGroup performLayout;
    private ViewGroup completeLayout;
    private ViewGroup repeatLayout;
    private ViewGroup holdLayout;
    private TextView performCount;
    private TextView completeCount;
    private TextView repeatCount;
    private TextView holdCount;
    private ImageButton performDecrementButton;
    private ImageButton performIncrementButton;
    private ImageButton completeDecrementButton;
    private ImageButton completeIncrementButton;
    private ImageButton repeatDecrementButton;
    private ImageButton repeatIncrementButton;
    private ProgressBar holdProgressBar;
    private ImageButton holdProgressButton;
    private Button editButton;
    private Button resetButton;
    private Button saveButton;
    private Exercise currentExercise;
    private ButtonClickListener buttonClickListener;
    private ValueAnimator holdProgressAnimator;

    public ExerciseProgressFragment() {
        // Required empty public constructor
    }

    /**
     * Create a new fragment with the current exercise.
     *
     * @param exercise current exercise to display
     * @return A new instance of the fragment
     */
    public static ExerciseProgressFragment newInstance(Exercise exercise) {
        ExerciseProgressFragment fragment = new ExerciseProgressFragment();
        fragment.setCurrentExercise(exercise);
        return fragment;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exercise_progress, container, false);

        performLayout = (ViewGroup) view.findViewById(R.id.performCountLayout);
        performDecrementButton = (ImageButton) performLayout.findViewById(R.id.decrementButton);
        performDecrementButton.setOnClickListener(this);
        performIncrementButton = (ImageButton) performLayout.findViewById(R.id.incrementButton);
        performIncrementButton.setOnClickListener(this);
        performCount = (TextView) performLayout.findViewById(R.id.exerciseCount);

        completeLayout = (ViewGroup) view.findViewById(R.id.completeCountLayout);
        completeDecrementButton = (ImageButton) completeLayout.findViewById(R.id.decrementButton);
        completeDecrementButton.setOnClickListener(this);
        completeIncrementButton = (ImageButton) completeLayout.findViewById(R.id.incrementButton);
        completeIncrementButton.setOnClickListener(this);
        completeCount = (TextView) completeLayout.findViewById(R.id.exerciseCount);

        repeatLayout = (ViewGroup) view.findViewById(R.id.repetitionsLayout);
        repeatDecrementButton = (ImageButton) repeatLayout.findViewById(R.id.decrementButton);
        repeatDecrementButton.setOnClickListener(this);
        repeatIncrementButton = (ImageButton) repeatLayout.findViewById(R.id.incrementButton);
        repeatIncrementButton.setOnClickListener(this);
        repeatCount = (TextView) repeatLayout.findViewById(R.id.exerciseCount);

        holdLayout = (ViewGroup) view.findViewById(R.id.holdLayout);
        holdCount = (TextView) holdLayout.findViewById(R.id.timeLabel);
        holdProgressBar = (ProgressBar) view.findViewById(R.id.holdProgressBar);
        holdProgressButton = (ImageButton) view.findViewById(R.id.holdProgressButton);
        holdProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleHoldProgressBar();
            }
        });

        editButton = (Button) view.findViewById(R.id.editExerciseButton);
        editButton.setOnClickListener(this);
        resetButton = (Button) view.findViewById(R.id.resetExerciseButton);
        resetButton.setOnClickListener(this);
        saveButton = (Button) view.findViewById(R.id.saveExerciseButton);
        saveButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayCurrentExercise();
    }

    public void setCurrentExercise(final Exercise exercise) {
        currentExercise = exercise;
    }

    public void displayCurrentExercise() {
        if (currentExercise != null && currentExercise.progress != null) {
            resetHoldProgressAnimation();

            final Resources resources = getResources();
            if (currentExercise.instructions.perform != null) {
                performLayout.setVisibility(View.VISIBLE);
                performCount.setText(String.format(
                        resources.getString(R.string.workoutCount),
                        currentExercise.progress.performCount,
                        currentExercise.progress.performMax));
            } else {
                performLayout.setVisibility(View.GONE);
            }

            if (currentExercise.instructions.complete != null) {
                completeLayout.setVisibility(View.VISIBLE);
                completeCount.setText(String.format(
                        resources.getString(R.string.completeCount),
                        currentExercise.progress.completeCount,
                        currentExercise.progress.completeMax));
            } else {
                completeLayout.setVisibility(View.GONE);
            }

            if (currentExercise.instructions.repeat != null) {
                repeatLayout.setVisibility(View.VISIBLE);
                repeatCount.setText(String.format(
                        resources.getString(R.string.repeatCount),
                        currentExercise.progress.repeatCount,
                        currentExercise.progress.repeatMax));
            } else {
                repeatLayout.setVisibility(View.GONE);
            }

            if (currentExercise.instructions.hold != null) {
                holdLayout.setVisibility(View.VISIBLE);
                holdCount.setText(String.format(
                        resources.getString(R.string.holdCount),
                        currentExercise.progress.holdCount,
                        currentExercise.progress.holdMax));
                float ratio = ((float) currentExercise.progress.holdCount
                        / currentExercise.progress.holdMax);
                holdProgressBar.setProgress((int) (ratio * 100));
            } else {
                holdLayout.setVisibility(View.GONE);
            }
        }
    }

    private void startHoldProgressTimer() {
        int start = holdProgressBar.getProgress();
        int duration = currentExercise.progress.holdMax - currentExercise.progress.holdCount;
        Log.d(TAG, "startHoldProgressTimer: start=" + start + ", duration=" + duration);
        holdProgressAnimator = ValueAnimator.ofInt(start, 100);
        holdProgressAnimator.setDuration(1000 * duration);
        holdProgressAnimator.setInterpolator(new LinearInterpolator());
        holdProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
                    final int value = (int) valueAnimator.getAnimatedValue();
                    currentExercise.progress.holdCount = (value * currentExercise.progress.holdMax) / 100;
                    holdProgressBar.setProgress(value);
                    holdCount.setText(String.format(
                            getResources().getString(R.string.holdCount),
                            currentExercise.progress.holdCount,
                            currentExercise.progress.holdMax));
                }
            }
        });
        holdProgressAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Log.d(TAG, "onAnimationEnd");
                holdProgressButton.setImageResource(android.R.drawable.ic_media_play);
                holdProgressBar.setProgress(0);
                currentExercise.progress.holdCount = 0;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                Log.d(TAG, "onAnimationCancel");
                animator.removeAllListeners();
                holdProgressButton.setImageResource(android.R.drawable.ic_media_play);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                // do nothing
            }
        });
        holdProgressAnimator.start();

        holdProgressButton.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void pauseHoldProgressAnimation() {
        if (holdProgressAnimator != null && holdProgressAnimator.isRunning()) {
            // unfortunately, pause is only available in api 19+, so use cancel for simplicity
            holdProgressAnimator.cancel();

            holdProgressButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void resetHoldProgressAnimation() {
        pauseHoldProgressAnimation();
        currentExercise.progress.holdCount = 0;
    }

    private void toggleHoldProgressBar() {
        if (holdProgressAnimator != null && holdProgressAnimator.isRunning()) {
            pauseHoldProgressAnimation();
        } else {
            startHoldProgressTimer();
        }
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
    public void onClick(View view) {
        if (view == editButton) {
            pauseHoldProgressAnimation();
            buttonClickListener.onEditButtonClicked();
        } else if (view == resetButton) {
            pauseHoldProgressAnimation();
            buttonClickListener.onResetButtonClicked();
        } else if (view == saveButton) {
            pauseHoldProgressAnimation();
            buttonClickListener.onSaveButtonClicked();
        } else if (currentExercise != null && currentExercise.progress != null) {
            final Exercise.Progress progress = currentExercise.progress;
            if (view.equals(performDecrementButton) && progress.performCount > 0) {
                --progress.performCount;
            } else if (view.equals(performIncrementButton) && progress.performCount < progress.performMax) {
                ++progress.performCount;
            } else if (view.equals(completeDecrementButton) && progress.completeCount > 0) {
                --progress.completeCount;
            } else if (view.equals(completeIncrementButton) && progress.completeCount < progress.completeMax) {
                ++progress.completeCount;
            } else if (view.equals(repeatDecrementButton) && progress.repeatCount > 0) {
                --progress.repeatCount;
            } else if (view.equals(repeatIncrementButton) && progress.repeatCount < progress.repeatMax) {
                ++progress.repeatCount;
            }

            displayCurrentExercise();
            checkExerciseComplete();
        }
    }

    private void checkExerciseComplete() {
        if (currentExercise != null && currentExercise.isDone()) {
            buttonClickListener.onExerciseComplete();
        }
    }

    /**
     * Listener for the activity to handle the "Edit", "Reset", and "Save" button clicks, and
     * when the exercise is complete (all fields have max value.)
     */
    public interface ButtonClickListener {
        void onEditButtonClicked();

        void onResetButtonClicked();

        void onSaveButtonClicked();

        void onExerciseComplete();
    }
}
