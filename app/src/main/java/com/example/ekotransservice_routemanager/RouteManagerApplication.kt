package com.example.ekotransservice_routemanager

import android.app.Application
import androidx.work.Configuration

class RouteManagerApplication: Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}