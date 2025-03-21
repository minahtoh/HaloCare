package com.example.halocare.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.halocare.MainActivity
import com.example.halocare.R

class MedicationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val medicationName = intent?.getStringExtra("medication_name") ?: "Medication"
        val medicationDosage = intent?.getStringExtra("medication_dosage") ?: ""

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "medication_channel")
            .setSmallIcon(R.drawable.baseline_medication_24)
            .setContentTitle("Time to take $medicationName!")
            .setContentText("Dosage: $medicationDosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.notify(medicationName.hashCode(), notification)
    }
}
