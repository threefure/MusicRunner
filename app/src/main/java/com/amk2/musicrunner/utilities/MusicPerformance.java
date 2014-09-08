package com.amk2.musicrunner.utilities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ktlee on 9/7/14.
 */
public class MusicPerformance implements Parcelable{

    public Integer duration;
    public Double distance;
    public Double calories;
    public Double performance;
    public String name;

    /*
    * create be ktlee
    * construct class by parcel
    */
    public MusicPerformance(Parcel p) {
        duration =    p.readInt();
        distance =    p.readDouble();
        calories =    p.readDouble();
        performance = p.readDouble();
        name =        p.readString();
    }

    public MusicPerformance (Integer dur, Double dis, Double cal, String n) {
        duration = dur;
        distance = dis;
        calories = cal;
        performance = calories*60/duration;
        name = n;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(duration);
        parcel.writeDouble(distance);
        parcel.writeDouble(calories);
        parcel.writeDouble(performance);
        parcel.writeString(name);
    }

    public static final Parcelable.Creator<MusicPerformance> CREATOR
            = new Parcelable.Creator<MusicPerformance>() {
        @Override
        public MusicPerformance createFromParcel (Parcel in) {
            return new MusicPerformance(in);
        }

        @Override
        public MusicPerformance[] newArray(int i) {
            return new MusicPerformance[i];
        }
    };
}
