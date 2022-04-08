package de.marmaro.krt.ffupdater.device

import android.os.Build
import android.os.Build.VERSION.SDK_INT

/**
 * This class makes SDK checks testable because Mockk can't mock/change Android classes.
 */
object DeviceSdkTester {
    val sdkInt = SDK_INT

    /**
     * API level 23
     */
    fun supportsAndroidMarshmallow(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.M
    }

    /**
     * API level 24
     */
    fun supportsAndroidNougat(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.N
    }

    /**
     * API level 26
     */
    fun supportsAndroidOreo(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.O
    }

    /**
     * API level 28
     */
    fun supportsAndroid9(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.P
    }

    /**
     * API level 29
     */
    fun supportsAndroid10(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.Q
    }

    /**
     * API level 30
     */
    fun supportsAndroid11(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.R
    }

    /**
     * API level 31
     */
    fun supportsAndroid12(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.S
    }
}