package com.example.ekotransservice_routemanager.WorkManager

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository

class UploadFilesWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    private val routeRepository: RouteRepository = RouteRepository.getInstance(appContext)

    @RequiresApi(Build.VERSION_CODES.O)

    override suspend fun doWork(): Result {
        // Do the work here--in this case, upload the images.
        routeRepository.uploadFilesAsync()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}