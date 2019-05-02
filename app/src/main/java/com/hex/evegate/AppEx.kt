package com.hex.evegate

import android.app.Application
import android.content.Context

class AppEx : Application() {

    companion object {
        @JvmStatic
        var instance: AppEx? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

//        TrafficCop.Builder()
//                // Set a threshold for downloads
//                .downloadWarningThreshold(Threshold.of(100, SizeUnit.KILOBYTES).per(1, TimeUnit.SECOND))
//                // Set a threshold for uploads
//                .uploadWarningThreshold(Threshold.of(100, SizeUnit.KILOBYTES).per(1, TimeUnit.SECOND))
//                // Register callbacks to be alerted when the threshold is reached
//                .alert(LogDataUsageAlertListener(), object : LogDataUsageAlertListener() {
//                    override fun alertThreshold(threshold: Threshold, dataUsage: DataUsage) {
//                        // Alert somehow!
//                        Toast.makeText(this@AppEx, dataUsage.humanReadableSize, Toast.LENGTH_SHORT).show()
//                    }
//                })
//                // Pass a string that uniquely identifies this instance.
//                .register("myTrafficCop", this).startMeasuring()
    }

    var shpHQ: Boolean
        get() {
            return getSharedPreferences(instance?.packageName, Context.MODE_PRIVATE)
                    ?.getBoolean("HighQuality", true)!!
        }
        set(value) {
            val sPref = getSharedPreferences(instance?.packageName, Context.MODE_PRIVATE)
            val ed = sPref?.edit()
            ed?.putBoolean("HighQuality", value)
            ed?.apply()
        }


}