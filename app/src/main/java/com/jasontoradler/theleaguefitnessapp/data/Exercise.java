package com.jasontoradler.theleaguefitnessapp.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * POJO encapsulating all data of a single workout (e.g. "Plank")
 * <p>
 * Typically includes description, instructions, and img; possibly video, if available.
 */
public class Exercise {
    public String[] description;
    public Instructions instructions;
    public Progress progress = new Progress();
    public Image img;
    public String video;

    public static Exercise copy(Exercise exercise) {
        Exercise copy = new Exercise();
        copy.description = new String[exercise.description.length];
        System.arraycopy(exercise.description, 0, copy.description, 0, copy.description.length);
        copy.instructions = Instructions.copy(exercise.instructions);
        copy.progress = Progress.copy(exercise.progress);
        copy.img = Image.copy(exercise.img);
        copy.video = exercise.video;
        return copy;
    }

    /**
     * Helper method to extract a number from a string. For example, "1 Time(s) a Day" would return
     * the number 1.
     *
     * @param str the input string which should contain at least one number.
     * @return the first number found in str
     */
    public static int extractNum(String str) {
        int num = 0;
        if (str != null && !str.isEmpty()) {
            final Matcher matcher = Pattern.compile("\\d+").matcher(str);
            if (matcher.find()) {
                num = Integer.parseInt(matcher.group());
            }
        }
        return num;
    }

    public void initProgress() {
        if (instructions != null) {
            progress.completeCount = 0;
            progress.completeMax = extractNum(instructions.complete);
            progress.performCount = 0;
            progress.performMax = extractNum(instructions.perform);
            progress.repeatCount = 0;
            progress.repeatMax = extractNum(instructions.repeat);
            progress.holdCount = 0;
            progress.holdMax = extractNum(instructions.hold);
        }
    }

    public void resetProgress() {
        progress.completeCount = 0;
        progress.performCount = 0;
        progress.repeatCount = 0;
        progress.holdCount = 0;
    }

    @JsonIgnore
    public boolean isDone() {
        boolean performDone = progress.performMax <= 0 || progress.performCount >= progress.performMax;
        boolean completeDone = progress.completeMax <= 0 || progress.completeCount >= progress.completeMax;
        boolean repeatDone = progress.repeatMax <= 0 || progress.repeatCount >= progress.repeatMax;
        // don't count holdCount since it's reset to zero during workout
        return performDone && completeDone && repeatDone;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "description=" + Arrays.toString(description) +
                ", instructions=" + instructions +
                ", progress=" + progress +
                ", img=" + img +
                ", video='" + video + '\'' +
                '}';
    }

    public static class Instructions {
        public String repeat; // "15 Times"
        public String hold; // "45 Seconds"
        public String complete; // "2 Sets"
        public String perform; // "1 Time(s) a Day

        public static Instructions copy(final Instructions instructions) {
            Instructions copy = new Instructions();
            if (instructions != null) {
                copy.repeat = instructions.repeat;
                copy.hold = instructions.hold;
                copy.complete = instructions.complete;
                copy.perform = instructions.perform;
            }
            return copy;
        }

        @Override
        public String toString() {
            return "Instructions{" +
                    "complete='" + complete + '\'' +
                    ", repeat='" + repeat + '\'' +
                    ", hold='" + hold + '\'' +
                    ", perform='" + perform + '\'' +
                    '}';
        }
    }

    public static class Progress {
        public int repeatCount;
        public int repeatMax;
        public int completeCount;
        public int completeMax;
        public int performCount;
        public int performMax;
        public int holdCount;
        public int holdMax;

        public static Progress copy(Progress progress) {
            Progress copy = new Progress();
            copy.repeatCount = progress.repeatCount;
            copy.repeatMax = progress.repeatMax;
            copy.completeCount = progress.completeCount;
            copy.completeMax = progress.completeMax;
            copy.performCount = progress.performCount;
            copy.performMax = progress.performMax;
            copy.holdCount = progress.holdCount;
            copy.holdMax = progress.holdMax;
            return copy;
        }

        @Override
        public String toString() {
            return "Progress{" +
                    "completeCount=" + completeCount +
                    ", repeatCount=" + repeatCount +
                    ", repeatMax=" + repeatMax +
                    ", completeMax=" + completeMax +
                    ", performCount=" + performCount +
                    ", performMax=" + performMax +
                    ", holdCount=" + holdCount +
                    ", holdMax=" + holdMax +
                    '}';
        }
    }

    public static class Image {
        public String src;
        public int height;

        public static Image copy(Image img) {
            Image copy = new Image();
            if (img != null) {
                copy.src = img.src;
                copy.height = img.height;
            }
            return copy;
        }

        @Override
        public String toString() {
            return "Image{" +
                    "height=" + height +
                    ", src='" + src + '\'' +
                    '}';
        }
    }
}
