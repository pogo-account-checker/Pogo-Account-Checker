package com.pogoaccountchecker.utils;

public final class Utils {
    private Utils() {}

    public static int randomWithRange(int min, int max)  {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
