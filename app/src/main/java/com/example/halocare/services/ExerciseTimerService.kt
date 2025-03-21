package com.example.halocare.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.halocare.MainActivity
import com.example.halocare.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val NOTIFICATION_CHANNEL_ID = "exercise_timer_channel"
const val NOTIFICATION_ID = 1

class ExerciseTimerService: Service() {
    private var job: Job? = null
    private var time = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, getNotification("Exercise Timer: 0s"))
        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(1000L)
                time++
                val notification = getNotification("Exercise Timer: ${time}s")
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            sendStopBroadcast()
            stopSelf()
            return START_NOT_STICKY
        }

        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        job?.cancel()
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    private fun sendStopBroadcast() {
        val intent = Intent("EXERCISE_TIMER_STOPPED")
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Exercise Timer",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun getNotification(contentText: String): android.app.Notification {
        val stopIntent = Intent(this, ExerciseTimerService::class.java).apply { action = "STOP_SERVICE" }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "daily_habits")
            putExtra("selected_tab", "Exercise")  // ✅ Pass selected tab info
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("HaloCare Exercise Timer")
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.baseline_directions_run_24)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }
}