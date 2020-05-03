package de.marmaro.krt.ffupdater.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class with useful helper methods.
 */
public class Utils {
    private static final String LOG_TAG = "Utils";

    /**
     * @param string string
     * @return if string is null, then return empty string.
     * if string is not null, return string.
     */
    @NonNull
    public static String convertNullToEmptyString(@Nullable String string) {
        if (string == null) {
            return "";
        }
        return string;
    }

    /**
     * @param millis wait x milliseconds and ignore InterruptedException
     */
    public static void sleepAndIgnoreInterruptedException(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "failed sleep", e);
        }
    }

    private static final String[] versionAndCodenames = new String[]{
            "1.0",
            "1.1",
            "1.5 Cupcake",
            "1.6 (Donut)",
            "2.0 (Eclair)",
            "2.0.1 (Eclair)",
            "2.1 (Eclair)",
            "2.2 (Froyo)",
            "2.3 (Gingerbread)",
            "2.3.3 (Gingerbread)",
            "3.0 (Honeycomb)",
            "3.1 (Honeycomb)",
            "3.2 (Honeycomb)",
            "4.0.1 (Ice Cream Sandwich)",
            "4.0.3 (Ice Cream Sandwich)",
            "4.1 (Jelly Bean)",
            "4.2 (Jelly Bean)",
            "4.3 (Jelly Bean)",
            "4.4 (KitKat)",
            "5.0 (Lollipop)",
            "5.1 (Lollipop)",
            "6.0 (Marshmallow)",
            "7.0 (Nougat)",
            "7.1 (Nougat)",
            "8.0.0 (Oreo)",
            "8.1.0 (Oreo)",
            "9 (Pie)",
            "10 (Android10)",
    };

    /**
     * @param apiLevel API Level
     * @return the Android version an its codename for the associated API Level
     */
    public static String getVersionAndCodenameFromApiLevel(int apiLevel) {
        if (apiLevel <= 0 || apiLevel >= versionAndCodenames.length) {
            return "API Level " + apiLevel;
        }
        return versionAndCodenames[apiLevel - 1];
    }
}
