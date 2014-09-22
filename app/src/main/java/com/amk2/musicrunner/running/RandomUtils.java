package com.amk2.musicrunner.running;

import java.util.Random;

/**
 * Created by tengchou on 9/21/14.
 */
public class RandomUtils {
    private static Random r = new Random();
    public static int randomInt(int range) {
        return(r.nextInt(range));
    }
    public static int randomIndex(Object[] array) {
        return(randomInt(array.length));
    }
    public static <T> T randomElement(T[] array) {
        return(array[randomIndex(array)]);
    }
    public static float randomFloat(int n) {
        return((float)Math.random()*n);
    }
}
