package com.amk2.musicrunner.utilities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ktlee on 9/7/14.
 */
public class SongPerformance implements Parcelable{

    public Integer duration;
    public Double distance;
    public Double calories;
    public Double performance;
    public Double speed;
    public String name;

    /*
    * create be ktlee
    * construct class by parcel
    */
    public SongPerformance(Parcel p) {
        duration    = p.readInt();
        distance    = p.readDouble();
        calories    = p.readDouble();
        performance = p.readDouble();
        speed       = p.readDouble();
        name        = p.readString();
    }

    public SongPerformance(Integer dur, Double dis, Double cal, String n) {
        duration    = dur;
        distance    = dis;
        calories    = cal;
        performance = calories*60/duration;
        speed       = distance*60 / duration;
        name        = n;
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
        parcel.writeDouble(speed);
        parcel.writeString(name);
    }

    public static final Parcelable.Creator<SongPerformance> CREATOR
            = new Parcelable.Creator<SongPerformance>() {
        @Override
        public SongPerformance createFromParcel (Parcel in) {
            return new SongPerformance(in);
        }

        @Override
        public SongPerformance[] newArray(int i) {
            return new SongPerformance[i];
        }
    };
}
