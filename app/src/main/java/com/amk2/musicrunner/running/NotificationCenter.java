package com.amk2.musicrunner.running;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.utilities.StringLib;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by ktlee on 5/11/14.
 */
public class NotificationCenter {
    Context context;
    private SoundPool mSoundPool;
    HashMap<String, Integer> soundMap;

    private SharedPreferences mSettingSharedPreferences;
    private Integer unitDistance;
    private Integer unitSpeedPace;
    private String language;

    public NotificationCenter(Context _context) {
        context = _context;
        // setting shared preference
        mSettingSharedPreferences = context.getSharedPreferences(SettingActivity.SETTING_SHARED_PREFERENCE, 0);
        unitDistance  = mSettingSharedPreferences.getInt(SettingActivity.DISTANCE_UNIT, SettingActivity.SETTING_DISTANCE_KM);
        unitSpeedPace = mSettingSharedPreferences.getInt(SettingActivity.SPEED_PACE_UNIT, SettingActivity.SETTING_PACE);
        language      = mSettingSharedPreferences.getString(SettingActivity.LANGUAGE, SettingActivity.SETTING_LANGUAGE_ENGLISH);

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
        //soundMap.put("sec", mSoundPool.load(context, R.raw.second, 1));
        soundMap.put("time", mSoundPool.load(context, R.raw.time, 1));
        soundMap.put("distance", mSoundPool.load(context, R.raw.distance, 1));
        soundMap.put("km", mSoundPool.load(context, R.raw.km, 1));
        soundMap.put("mi", mSoundPool.load(context, R.raw.mile, 1));
        soundMap.put("dot", mSoundPool.load(context, R.raw.point, 1));
        soundMap.put("avgSpeed", mSoundPool.load(context, R.raw.averagespeed, 1));
        soundMap.put("avgPace", mSoundPool.load(context, R.raw.averagepace, 1));
        soundMap.put("every", mSoundPool.load(context, R.raw.every, 1));
        soundMap.put("consumeCalories", mSoundPool.load(context, R.raw.calorieburned, 1));
        soundMap.put("kcal", mSoundPool.load(context, R.raw.kcal, 1));
        soundMap.put("hundred", mSoundPool.load(context, R.raw.hundred, 1));
        soundMap.put("thousand", mSoundPool.load(context, R.raw.thousand, 1));

        if (language.equals(SettingActivity.SETTING_LANGUAGE_ENGLISH)) {
            soundMap.put("11", mSoundPool.load(context, R.raw.eleven, 1));
            soundMap.put("12", mSoundPool.load(context, R.raw.twelve, 1));
            soundMap.put("13", mSoundPool.load(context, R.raw.thirteen, 1));
            soundMap.put("14", mSoundPool.load(context, R.raw.fourteen, 1));
            soundMap.put("15", mSoundPool.load(context, R.raw.fifteen, 1));
            soundMap.put("16", mSoundPool.load(context, R.raw.sixteen, 1));
            soundMap.put("17", mSoundPool.load(context, R.raw.seventeen, 1));
            soundMap.put("18", mSoundPool.load(context, R.raw.eighteen, 1));
            soundMap.put("19", mSoundPool.load(context, R.raw.nineteen, 1));
            soundMap.put("20", mSoundPool.load(context, R.raw.twenty, 1));
            soundMap.put("30", mSoundPool.load(context, R.raw.thirty, 1));
            soundMap.put("40", mSoundPool.load(context, R.raw.forty, 1));
            soundMap.put("50", mSoundPool.load(context, R.raw.fifty, 1));
            soundMap.put("60", mSoundPool.load(context, R.raw.sixty, 1));
            soundMap.put("70", mSoundPool.load(context, R.raw.seventy, 1));
            soundMap.put("80", mSoundPool.load(context, R.raw.eighty, 1));
            soundMap.put("90", mSoundPool.load(context, R.raw.ninety, 1));
        } else if (language.equals(SettingActivity.SETTING_LANGUAGE_JAPANESE)) {
            soundMap.put("20", mSoundPool.load(context, R.raw.twenty, 1));
            soundMap.put("30", mSoundPool.load(context, R.raw.thirty, 1));
            soundMap.put("40", mSoundPool.load(context, R.raw.forty, 1));
            soundMap.put("50", mSoundPool.load(context, R.raw.fifty, 1));
            soundMap.put("60", mSoundPool.load(context, R.raw.sixty, 1));
            soundMap.put("70", mSoundPool.load(context, R.raw.seventy, 1));
            soundMap.put("80", mSoundPool.load(context, R.raw.eighty, 1));
            soundMap.put("90", mSoundPool.load(context, R.raw.ninety, 1));
        }
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
        private String reportString;

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

            //play distance number
            if (language.equals(SettingActivity.SETTING_LANGUAGE_ENGLISH)) {
                playNumberInEng(distance);
            } else if (language.equals(SettingActivity.SETTING_LANGUAGE_JAPANESE)) {
                playNumberInJP(distance);
            } else {
                playNumber(distance);
            }
            if (unitDistance == SettingActivity.SETTING_DISTANCE_KM) {
                this.play(soundMap.get("km"), 1200);
            } else {
                this.play(soundMap.get("mi"), 1200);
            }
        }

        public void reportCalories () {
            this.play(soundMap.get("consumeCalories"), 2000);

            //play calorie number
            if (language.equals(SettingActivity.SETTING_LANGUAGE_ENGLISH)) {
                playNumberInEng(calories);
            } else if (language.equals(SettingActivity.SETTING_LANGUAGE_JAPANESE)) {
                playNumberInJP(calories);
            } else {
                playNumber(calories);
            }
            this.play(soundMap.get("kcal"), 1500);
        }

        /*
         * reportSpeed: report the current speed
         */
        public void reportSpeed () {
            if (unitSpeedPace == SettingActivity.SETTING_PACE) { // reporting pace sounds
                this.play(soundMap.get("avgPace"), 1200);
                this.play(soundMap.get("every"), 700);

                if (unitDistance == SettingActivity.SETTING_DISTANCE_KM) {
                    this.play(soundMap.get("km"), 1000);
                } else {
                    this.play(soundMap.get("mi"), 1000);
                }

                if (language.equals(SettingActivity.SETTING_LANGUAGE_ENGLISH)) {
                    playNumberInEng(speed);
                } else if (language.equals(SettingActivity.SETTING_LANGUAGE_JAPANESE)) {
                    playNumberInJP(speed);
                } else {
                    playNumber(speed);
                }

                this.play(soundMap.get("min"), 1000);
            } else { // reporting speed sounds
                this.play(soundMap.get("avgSpeed"), 1200);
                // play speed
                if (language.equals(SettingActivity.SETTING_LANGUAGE_ENGLISH)) {
                    playNumberInEng(speed);
                } else if (language.equals(SettingActivity.SETTING_LANGUAGE_JAPANESE)) {
                    playNumberInJP(speed);
                } else {
                    playNumber(speed);
                }

                if (unitDistance == SettingActivity.SETTING_DISTANCE_KM) {
                    this.play(soundMap.get("km"), 1000);
                } else {
                    this.play(soundMap.get("mi"), 1000);
                }
            }
        }

        private void playNumberInEng (Double number) {
            boolean isLTTen = false; //is larger than 10
            if (number >= 1000) {
                toPlay = number.intValue() / 1000;
                this.play(soundMap.get(toPlay.toString()), 700);
                this.play(soundMap.get("thousand"), 700);
                number -= (toPlay.doubleValue() * 1000);
                isLTTen = true;
            }
            if (number >= 100) {
                toPlay = number.intValue() / 100;
                this.play(soundMap.get(toPlay.toString()), 700);
                this.play(soundMap.get("hundred"), 700);
                number -= (toPlay.doubleValue() * 100);
                isLTTen = true;
            }
            if (number >= 20) {
                toPlay = number.intValue() / 10;
                toPlay *= 10;
                this.play(soundMap.get(toPlay.toString()), 700);
                number -= (toPlay.doubleValue());
                isLTTen = true;
            }
            if (number >= 10) {
                // 10 ~ 19
                toPlay = number.intValue();
                this.play(soundMap.get(toPlay.toString()), 700);

                // ten
                //this.play(soundMap.get("10"), 700);
                number -= toPlay.doubleValue();
                isLTTen = true;
            }
            if (number > 0) {
                // first digit
                toPlay = number.intValue() % 10;
                if (toPlay > 0) {
                    this.play(soundMap.get(toPlay.toString()), 700);
                } else {
                    if (!isLTTen) {
                        this.play(soundMap.get(toPlay.toString()), 700);
                    }
                }

                reportString = number.toString();
                indexOfDot = reportString.indexOf(".");
                if (indexOfDot > 0) {
                    reportString = StringLib.truncateDoubleString(reportString, 2);

                    // dot
                    this.play(soundMap.get("dot"), 700);

                    // 小數點後第一位
                    this.play(soundMap.get(reportString.charAt(indexOfDot + 1) + ""), 700);

                    // 小數點後第二位
                    if (reportString.length() > indexOfDot + 2) {
                        this.play(soundMap.get(reportString.charAt(indexOfDot + 2) + ""), 700);
                    }
                }
            }

            if (number == 0) {
                this.play(soundMap.get("0"), 700);
            }
        }

        private void playNumberInJP (Double number) {
            boolean isLTTen = false; //is larger than 10
            if (number >= 1000) {
                toPlay = number.intValue() / 1000;
                this.play(soundMap.get(toPlay.toString()), 700);
                this.play(soundMap.get("thousand"), 700);
                number -= (toPlay.doubleValue() * 1000);
                isLTTen = true;
            }
            if (number >= 100) {
                toPlay = number.intValue() / 100;
                this.play(soundMap.get(toPlay.toString()), 700);
                this.play(soundMap.get("hundred"), 700);
                number -= (toPlay.doubleValue() * 100);
                isLTTen = true;
            }
            if (number >= 20) {
                toPlay = number.intValue() / 10;
                toPlay *= 10;
                this.play(soundMap.get(toPlay.toString()), 700);
                number -= (toPlay.doubleValue());
                isLTTen = true;
            }
            if (number >= 10) {
                // second digit
                toPlay = number.intValue() / 10;
                this.play(soundMap.get(toPlay.toString()), 700);

                // ten
                this.play(soundMap.get("10"), 700);
                isLTTen = true;
            }
            if (number > 0) {
                // first digit
                toPlay = number.intValue() % 10;
                if (toPlay > 0) {
                    this.play(soundMap.get(toPlay.toString()), 700);
                } else {
                    if (!isLTTen) {
                        this.play(soundMap.get(toPlay.toString()), 700);
                    }
                }

                reportString = number.toString();
                indexOfDot = reportString.indexOf(".");
                if (indexOfDot > 0) {
                    reportString = StringLib.truncateDoubleString(reportString, 2);

                    // dot
                    this.play(soundMap.get("dot"), 700);

                    // 小數點後第一位
                    this.play(soundMap.get(reportString.charAt(indexOfDot + 1) + ""), 700);

                    // 小數點後第二位
                    if (reportString.length() > indexOfDot + 2) {
                        this.play(soundMap.get(reportString.charAt(indexOfDot + 2) + ""), 700);
                    }
                }
            }

            if (number == 0) {
                this.play(soundMap.get("0"), 700);
            }
        }

        private void playNumber (Double number) {
            boolean isLTTen = false; //is larger than 10
            if (number >= 1000) {
                toPlay = number.intValue() / 1000;
                this.play(soundMap.get(toPlay.toString()), 700);
                this.play(soundMap.get("thousand"), 700);
                number -= (toPlay.doubleValue() * 1000);
                isLTTen = true;
            }
            if (number >= 100) {
                toPlay = number.intValue() / 100;
                this.play(soundMap.get(toPlay.toString()), 700);
                this.play(soundMap.get("hundred"), 700);
                number -= (toPlay.doubleValue() * 100);
                isLTTen = true;
            }
            if (number >= 10) {
                // second digit
                toPlay = number.intValue() / 10;
                this.play(soundMap.get(toPlay.toString()), 700);

                // ten
                this.play(soundMap.get("10"), 700);
                isLTTen = true;
            }
            if (number > 0) {
                // first digit
                toPlay = number.intValue() % 10;
                if (toPlay > 0) {
                    this.play(soundMap.get(toPlay.toString()), 700);
                } else {
                    if (!isLTTen) {
                        this.play(soundMap.get(toPlay.toString()), 700);
                    }
                }

                reportString = number.toString();
                indexOfDot = reportString.indexOf(".");
                if (indexOfDot > 0) {
                    reportString = StringLib.truncateDoubleString(reportString, 2);

                    // dot
                    this.play(soundMap.get("dot"), 700);

                    // 小數點後第一位
                    this.play(soundMap.get(reportString.charAt(indexOfDot + 1) + ""), 700);

                    // 小數點後第二位
                    if (reportString.length() > indexOfDot + 2) {
                        this.play(soundMap.get(reportString.charAt(indexOfDot + 2) + ""), 700);
                    }
                }
            }

            if (number == 0) {
                this.play(soundMap.get("0"), 700);
            }
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
