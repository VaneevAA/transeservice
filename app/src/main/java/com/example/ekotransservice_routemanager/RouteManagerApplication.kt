package com.example.ekotransservice_routemanager

import android.app.Application
import androidx.work.*
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.example.ekotransservice_routemanager.WorkManager.UploadFilesWorker
import java.util.concurrent.TimeUnit

class RouteManagerApplication: Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        CaocConfig.Builder.create()
            .enabled(true)
            .trackActivities(true)
            .errorActivity(ErrorActivity::class.java)
            .apply()

        //region WorkManager
        // Work manager: configure schedule and rules for periodic files upload
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val uploadWorkRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<UploadFilesWorker>(45, TimeUnit.MINUTES)
                .addTag("uploadFiles")
                .setConstraints(constraints)
                .build()
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueueUniquePeriodicWork("uploadFiles",ExistingPeriodicWorkPolicy.KEEP,uploadWorkRequest)
        //workManager.pruneWork()
        //workManager.cancelAllWork()
        //endregion
    }
}