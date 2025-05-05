package com.example.halocare

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.halocare.services.scheduleDailyResetDosesWorker
import com.example.halocare.viewmodel.MainRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HaloCareApp : Application(){

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Manually initialize WorkManager since the default initializer is disabled
        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
        WorkManager.initialize(this, config)

        // Schedule the worker to reset doses at 4 AM
        scheduleDailyResetDosesWorker(applicationContext)
    }
}
