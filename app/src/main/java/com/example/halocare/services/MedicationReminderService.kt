package com.example.halocare.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.halocare.R
import com.example.halocare.ui.models.Medication
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class MedicationReminderService {
}

/**
 * Calculates dose times from firstDoseTime and frequency.
 * Caps any time exactly at midnight to 23:00.
 */
fun calculateDoseTimes(firstDoseTime: LocalTime, frequency: Int): List<LocalTime> {
    return List(frequency) { i ->
        val interval = 24 / frequency
        val calculatedTime = firstDoseTime.plusHours((i * interval).toLong())
        if (calculatedTime == LocalTime.MIDNIGHT) {
            LocalTime.of(23, 0)
        } else {
            calculatedTime
        }
    }
}

/**
 * Schedules notifications for a medication's dose times.
 */
fun scheduleMedicationReminders(context: Context, medication: Medication) {
    if (!medication.isReminderOn) return

    createNotificationChannel(context)

    val today = LocalDate.now()
    if (!medication.prescribedDays.contains(today)) return

    val now = LocalTime.now()
    val doseTimes = calculateDoseTimes(medication.firstDoseTime, medication.frequency)

    doseTimes.forEachIndexed { index, doseTime ->
        if (now.isBefore(doseTime)) {
            val delayMillis = ChronoUnit.MILLIS.between(now, doseTime)

            val data = workDataOf(
                "medicationName" to medication.name,
                "notificationId" to (medication.medicationId * 10 + index)
            )

            val request = OneTimeWorkRequestBuilder<MedicationNotificationWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "medication_${medication.medicationId}_$index",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}

/**
 * Worker that sends the notification.
 */
class MedicationNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        val medicationName = inputData.getString("medicationName") ?: return Result.failure()
        val notificationId = inputData.getInt("notificationId", 0)

        val notification = NotificationCompat.Builder(applicationContext, "medication_channel")
            .setSmallIcon(R.drawable.baseline_medication_24) // Replace with your icon
            .setContentTitle("Time to take $medicationName")
            .setContentText("Tap to log your dose.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
        return Result.success()
    }
}

/**
 * Creates the notification channel if necessary.
 */
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "medication_channel",
            "Medication Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for scheduled medication doses"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}


// Function to schedule a notification based on doseTimes
@Composable
fun scheduleDoseNotifications(context: Context, medication: Medication, notificationEnabled: Boolean) {
    // Only schedule notifications if the user has enabled them
    if (!notificationEnabled) return

    val doseTimes = remember(medication.firstDoseTime, medication.frequency) {
        List(medication.frequency) { i ->
            val calculatedTime = medication.firstDoseTime.plusHours((i * (24 / medication.frequency)).toLong())

            // Check if the calculated time exceeds midnight, and if so, set it to 23:00
            if (calculatedTime.hour == 0 && calculatedTime.minute == 0) {
                calculatedTime.withHour(23).withMinute(0)
            } else {
                calculatedTime
            }
        }
    }

    // Now set up notifications based on the doseTimes
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Go through the doseTimes and schedule notifications
    doseTimes.forEachIndexed { index, doseTime ->
        // Check if the current time is less than the dose time (if yes, schedule)
        val currentTime = LocalTime.now()

        if (currentTime.isBefore(doseTime)) {
            val delay = ChronoUnit.MILLIS.between(currentTime, doseTime)

            // Create a notification
            val notification = NotificationCompat.Builder(context, "medication_channel")
                .setContentTitle("Time to take ${medication.name}")
                .setContentText("It's time to take your dose of ${medication.name}")
                .setSmallIcon(R.drawable.baseline_medication_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            // Schedule notification using WorkManager or AlarmManager
            // This is where you would trigger the actual notification at the calculated time
            // You can use WorkManager here if needed to schedule the notification at the exact time
            val workRequest = OneTimeWorkRequestBuilder<MedicationNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("notification" to notification))
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}