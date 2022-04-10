package de.marmaro.krt.ffupdater.download

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.AnyThread
import androidx.annotation.RequiresApi
import de.marmaro.krt.ffupdater.device.DeviceSdkTester

object NetworkUtil {
    @Deprecated("ich sollte diese Methode durch try/catch ersetzen, weil unzuverlässig")
    @AnyThread
    fun isInternetUnavailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return false
        // return !isInternetAvailable(cm)
    }

    private fun isInternetAvailable(cm: ConnectivityManager): Boolean {
        return if (DeviceSdkTester.supportsAndroid10()) {
            isNetworkConnected(cm)
        } else {
            isNetworkConnectedOldWay(cm)
        }
    }

    /**
     * https://gist.github.com/Farbklex/f84029889444ee9c52a331a7e2bd10d2
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isNetworkConnected(cm: ConnectivityManager): Boolean {
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * How AndroidX WorkManager checks for internet access:
     * https://android.googlesource.com/platform/frameworks/support.git/+/refs/heads/androidx-main/
     * work/work-runtime/src/main/java/androidx/work/impl/constraints/trackers/NetworkStateTracker.java#135
     */
    @Suppress("DEPRECATION")
    private fun isNetworkConnectedOldWay(cm: ConnectivityManager): Boolean {
        return cm.activeNetworkInfo?.isConnected == true
    }

    fun isNetworkUnmetered(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return !cm.isActiveNetworkMetered
    }

    fun isNetworkMetered(context: Context): Boolean = !isNetworkUnmetered(context)
}