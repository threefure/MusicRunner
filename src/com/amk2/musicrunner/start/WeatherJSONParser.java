package com.amk2.musicrunner.start;

import android.util.JsonReader;
import com.amk2.musicrunner.start.WeatherModel.WeatherEntry;
import com.amk2.musicrunner.start.WeatherModel.WeatherWeekEntry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daz on 2014/4/27.
 */
public class WeatherJSONParser {
    public static WeatherEntry read(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readWeather(reader);
        } finally {
            reader.close();
        }
    }

    public static ArrayList<WeatherWeekEntry> readWeek(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readWeatherWeek(reader);
        } finally {
            reader.close();
        }
    }

    public static WeatherEntry readWeather(JsonReader reader) throws IOException {
        String uv = "";
        String condition = "";
        String temperature = "";
        String feeling = "";
        String chanceOfRain = "";

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("uv")) {
                uv = reader.nextString();
            } else if (name.equals("condition")) {
                condition = reader.nextString();
            } else if (name.equals("maxT")) {
                temperature = reader.nextString();
            } else if (name.equals("feeling")) {
                feeling = reader.nextString();
            } else if (name.equals("chance-of-rain")) {
                chanceOfRain = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new WeatherEntry(uv, condition, temperature, feeling, chanceOfRain);
    }

    public static ArrayList<WeatherWeekEntry> readWeatherWeek(JsonReader reader) throws IOException {
        ArrayList<WeatherWeekEntry> weatherWeekEntryList = new ArrayList<WeatherWeekEntry>();

        reader.beginArray();
        while (reader.hasNext()) {
            weatherWeekEntryList.add(getWeatherWeekData(reader));
        }
        reader.endArray();
        return weatherWeekEntryList;
    }
    public static WeatherWeekEntry getWeatherWeekData(JsonReader reader) throws IOException {
        String month = "";
        String date = "";
        String day = "";
        String condition = "";
        String max_t = "";
        String min_t = "";

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("month")) {
                month = reader.nextString();
            } else if (name.equals("date")) {
                date = reader.nextString();
            } else if (name.equals("day")) {
                day = reader.nextString();
            } else if (name.equals("condition")) {
                condition = reader.nextString();
            } else if (name.equals("maxT")) {
                max_t = reader.nextString();
            } else if (name.equals("minT")) {
                min_t = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new WeatherWeekEntry(month, date, day, condition, max_t, min_t);
    }
}
