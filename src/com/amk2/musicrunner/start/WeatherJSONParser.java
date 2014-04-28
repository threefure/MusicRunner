package com.amk2.musicrunner.start;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
}
