package com.amk2.musicrunner.running;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.amk2.musicrunner.R;

import java.util.HashMap;

import static com.amk2.musicrunner.running.RunningActivity.truncateDoubleString;

/**
 * Created by ktlee on 5/11/14.
 */
public class NotificationCenter {
    Context context;
    private SoundPool mSoundPool;
    HashMap<String, Integer> soundMap;

    public NotificationCenter(Context _context) {
        context = _context;
        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap = new HashMap<String, Integer>();

        soundMap.put("0", mSoundPool.load(context, R.raw.zero, 1));
        soundMap.put("1", mSoundPool.load(context, R.raw.one, 1));
        soundMap.put("2", mSoundPool.load(context, R.raw.two, 1));
        soundMap.put("3", mSoundPool.load(context, R.raw.three, 1));
        soundMap.put("4", mSoundPool.load(context, R.raw.four, 1));
        soundMap.put("5", mSoundPool.load(context, R.raw.five, 1));
        soundMap.put("6", mSoundPool.load(context, R.raw.six, 1));
        soundMap.put("7", mSoundPool.load(context, R.raw.seven, 1));
        soundMap.put("8", mSoundPool.load(context, R.raw.eight, 1));
        soundMap.put("9", mSoundPool.load(context, R.raw.nine, 1));
        soundMap.put("10", mSoundPool.load(context, R.raw.ten, 1));
        soundMap.put("min", mSoundPool.load(context, R.raw.minute, 1));
        soundMap.put("sec", mSoundPool.load(context, R.raw.second, 1));
        soundMap.put("time", mSoundPool.load(context, R.raw.time, 1));
        soundMap.put("distance", mSoundPool.load(context, R.raw.distance, 1));
        soundMap.put("unit", mSoundPool.load(context, R.raw.km, 1));
        soundMap.put("dot", mSoundPool.load(context, R.raw.point, 1));
        soundMap.put("avgSpeed", mSoundPool.load(context, R.raw.average_pace, 1));
        soundMap.put("every", mSoundPool.load(context, R.raw.every, 1));
        soundMap.put("consumeCalories", mSoundPool.load(context, R.raw.consumed_calories, 1));
        soundMap.put("kcal", mSoundPool.load(context, R.raw.kcal, 1));
    }

    public void notifyStatus (int min, int sec, Double distance, Double speed, Double calories) {
        Thread routine = new Thread(new NotificationRoutine(min, sec, distance, speed, calories));
        routine.start();
    }

    private class NotificationRoutine implements Runnable{
        private int min;
        private int sec;
        private int indexOfDot;
        private Integer toPlay;
        private Double distance;
        private Double speed;
        private Double calories;
        private String distanceString;
        private String speedString;

        public NotificationRoutine (int _min, int _sec, Double _distance, Double _speed, Double _calories) {
            this.min = _min;
            this.sec = _sec;
            this.distance = _distance;
            this.speed = _speed;
            this.calories = _calories;
        }
        @Override
        public void run () {
            reportTime();
            reportDistance();
            reportCalories();
            reportSpeed();
        }
        /*
         * reportTime(): report current time
         */
        public void reportTime () {
            this.play(soundMap.get("time"), 1200);
            if (min >= 20) {
                // second digit
                toPlay = min / 10;
                this.play(soundMap.get(toPlay.toString()), 700);
            }
            if (min > 10) { // playing larger than and equal to 20
                // ten
                this.play(soundMap.get("10"), 700);

                // first digit
                toPlay = min % 10;
                if (toPlay > 0) {
                    this.play(soundMap.get(toPlay.toString()), 700);
                }

                // minute
                this.play(soundMap.get("min"), 1000);
            } else if (min > 0 && min <= 10) { // playing less than 10
                // first digit
                toPlay = min;
                this.play(soundMap.get(toPlay.toString()), 700);

                // minute
                this.play(soundMap.get("min"), 1000);
            }
        }
        /*
         * reportTime(): report current time
         */
        public void reportDistance () {
            this.play(soundMap.get("distance"), 1200);
            if (distance >= 20) {
                // second digit
                toPlay = distance.intValue() / 10;
                this.play(soundMap.get(toPlay.toString()), 700);
            }
            if (distance >= 10) {
                // ten
                this.play(soundMap.get("10"), 700);
            }
            if (distance > 0) {
                // first digit
                toPlay = distance.intValue() % 10;
                this.play(soundMap.get(toPlay.toString()), 700);

                distanceString = distance.toString();
                indexOfDot = distanceString.indexOf(".");
                if (indexOfDot > 0) {
                    distanceString = truncateDoubleString(distanceString, 2);

                    // dot
                    this.play(soundMap.get("dot"), 1000);

                    // 小數點後第一位
                    this.play(soundMap.get(distanceString.charAt(indexOfDot + 1) + ""), 700);

                    // 小數點後第二位
                    if ( distanceString.length() > indexOfDot + 2) {
                        this.play(soundMap.get(distanceString.charAt(indexOfDot + 2) + ""), 700);
                    }
                }
            }

            this.play(soundMap.get("unit"), 1200);
        }

        public void reportCalories () {
            this.play(soundMap.get("consumeCalories"), 2000);

            if (calories >= 20) {
                // second digit
                toPlay = calories.intValue() / 10;
                this.play(soundMap.get(toPlay.toString()), 700);
            }
            if (calories >= 10) {
                // ten
                this.play(soundMap.get("10"), 700);
            }
            if (calories > 0) {
                // first digit
                toPlay = calories.intValue() % 10;
                this.play(soundMap.get(toPlay.toString()), 700);

                speedString = calories.toString();
                indexOfDot = speedString.indexOf(".");
                if (indexOfDot > 0) {
                    speedString = truncateDoubleString(speedString, 2);

                    // dot
                    this.play(soundMap.get("dot"), 700);

                    // 小數點後第一位
                    this.play(soundMap.get(speedString.charAt(indexOfDot + 1) + ""), 700);

                    // 小數點後第二位
                    if ( speedString.length() > indexOfDot + 2) {
                        this.play(soundMap.get(speedString.charAt(indexOfDot + 2) + ""), 700);
                    }
                }
            }

            this.play(soundMap.get("kcal"), 1500);
        }

        /*
         * reportSpeed: report the current speed
         */
        public void reportSpeed () {
            this.play(soundMap.get("avgSpeed"), 1200);
            this.play(soundMap.get("every"), 700);
            this.play(soundMap.get("unit"), 1000);

            if (speed >= 20) {
                // second digit
                toPlay = speed.intValue() / 10;
                this.play(soundMap.get(toPlay.toString()), 700);
            }
            if (speed >= 10) {
                // ten
                this.play(soundMap.get("10"), 700);
            }
            if (speed > 0) {
                // first digit
                toPlay = speed.intValue() % 10;
                this.play(soundMap.get(toPlay.toString()), 700);

                speedString = speed.toString();
                indexOfDot = speedString.indexOf(".");
                if (indexOfDot > 0) {
                    speedString = truncateDoubleString(speedString, 2);

                    // dot
                    this.play(soundMap.get("dot"), 700);

                    // 小數點後第一位
                    this.play(soundMap.get(speedString.charAt(indexOfDot + 1) + ""), 700);

                    // 小數點後第二位
                    if ( speedString.length() > indexOfDot + 2) {
                        this.play(soundMap.get(speedString.charAt(indexOfDot + 2) + ""), 700);
                    }
                }
            }

            this.play(soundMap.get("min"), 1000);
        }

        /*
         * streamId: id of sound stream
         * sleepDuration: sleeping duration in minisec
         */
        public void play(int streamId, int sleepDuration) {
            mSoundPool.play(streamId, 1, 1, 0, 0, 1);
            try {
                Thread.sleep(sleepDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


}
