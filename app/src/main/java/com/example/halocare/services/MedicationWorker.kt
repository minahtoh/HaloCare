package com.example.halocare.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.halocare.viewmodel.MainRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@HiltWorker
class MedicationResetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MainRepository // Injected
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val allMeds = repository.getAllMedications()
        allMeds.collect { list ->
            list.forEach { med ->
                repository.updateMedication(med.copy(dosesUsedToday = 0))
            }
        }
        return Result.success()
    }
}

fun scheduleDailyResetDosesWorker(context: Context) {
    val resetDosesRequest = PeriodicWorkRequestBuilder<MedicationResetWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(getDelayUntil4AM(), TimeUnit.MILLISECONDS) // Delay until 4 AM
        .addTag("ResetDosesWorker")
        .build()

    WorkManager.getInstance(context).enqueue(resetDosesRequest)
}

private fun getDelayUntil4AM(): Long {
    val currentTime = LocalTime.now()
    val fourAM = LocalTime.of(4, 0)
    val duration = Duration.between(currentTime, fourAM)

    return if (duration.isNegative) {
        // If it's already past 4 AM, calculate the delay for the next day
        Duration.between(currentTime, fourAM.plusHours(24)).toMillis()
    } else {
        // Otherwise, use the remaining time until 4 AM today
        duration.toMillis()
    }
}


