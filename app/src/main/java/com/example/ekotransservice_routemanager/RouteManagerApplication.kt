package com.example.ekotransservice_routemanager

import android.app.Application
import androidx.work.Configuration
import cat.ereza.customactivityoncrash.config.CaocConfig

class RouteManagerApplication: Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        CaocConfig.Builder.create()
            .enabled(true)
            .trackActivities(true)
            .errorActivity(ErrorActivity::class.java)
            .apply()
    }
}