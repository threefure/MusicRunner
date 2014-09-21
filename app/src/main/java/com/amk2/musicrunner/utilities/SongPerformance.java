package com.amk2.musicrunner.utilities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Created by ktlee on 9/7/14.
 */
public class SongPerformance implements Parcelable, Comparator<SongPerformance>, Comparable<SongPerformance>{

    public Integer duration;
    public Integer times;
    public Integer songId;
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
        times       = p.readInt();
        duration    = p.readInt();
        songId      = p.readInt();
        distance    = p.readDouble();
        calories    = p.readDouble();
        performance = p.readDouble();
        speed       = p.readDouble();
        name        = p.readString();
    }

    public SongPerformance(Integer dur, Double dis, Double cal, String n) {
        songId      = -1;
        times       = 1;
        duration    = dur;
        distance    = dis;
        calories    = cal;
        performance = calories*60 / duration;
        speed       = distance*60 / duration;
        name        = n;
    }
    public SongPerformance(Integer dur, Double dis, Double cal, Double sp, String n) {
        songId      = -1;
        times       = 1;
        duration    = dur;
        distance    = dis;
        calories    = cal;
        performance = calories*60 / duration;
        speed       = sp;
        name        = n;
    }

    public void addSongRecord (Integer dur, Double dis, Double cal) {
        times ++;
        duration += dur;
        distance += dis;
        calories += cal;
        speed     = distance*60 / duration;
    }

    public void setSongId (Integer id) {
        songId = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(times);
        parcel.writeInt(duration);
        parcel.writeInt(songId);
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

    @Override
    public int compare(SongPerformance songPerformance, SongPerformance songPerformance2) {
        return songPerformance2.calories.compareTo(songPerformance.calories);
    }

    @Override
    public int compareTo(SongPerformance songPerformance) {
        return songPerformance.calories.compareTo(this.calories);
    }
}
