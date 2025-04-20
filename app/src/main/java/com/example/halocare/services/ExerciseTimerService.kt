package com.example.halocare.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.halocare.MainActivity
import com.example.halocare.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val NOTIFICATION_CHANNEL_ID = "exercise_timer_channel"


@AndroidEntryPoint
class ExerciseTimerService: Service() {
    @Inject // Tell Hilt to inject the repository instance here
    lateinit var timerRepository: TimerRepository
    private val serviceJob = SupervisorJob()
    private val timerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null
    private var currentTime = 0

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "ExerciseTimerChannel"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        const val EXTRA_ELAPSED_TIME = "elapsed_time"
        const val BROADCAST_ACTION_STOPPED = "EXERCISE_TIMER_STOPPED"
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        currentTime = timerRepository.currentTimeSeconds.value
        Log.d("ExerciseTimerService", "[Hilt] Initial time synced from repo: $currentTime")

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, getNotification("Exercise Timer: 0s"))
        Log.d("ExerciseTimerService", "Service Created and Foreground Started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                Log.d("ExerciseTimerService", "[Hilt] Stopping service, final time: $currentTime")
                stopTimer()
                sendStopBroadcast(
                //    currentTime
                )
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startTimerIfNotRunning()
                return START_STICKY
            }
        }
    }

    private fun startTimerIfNotRunning() {
        if (timerJob?.isActive == true) return
        Log.d("ExerciseTimerService", "[Hilt] Starting internal timer.")
        currentTime = timerRepository.currentTimeSeconds.value // Sync time on start

        timerJob = timerScope.launch {
            while (true) {
                delay(1000L)
                currentTime++
                // Update repository (which persists periodically)
                timerRepository.updateTimeAndPersist(currentTime)
                timerRepository.isTimerRunning(isTimerRunning())
                // Update notification
                val notification = getNotification("Exercise Timer: ${formatTime(currentTime)}")
                getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun isTimerRunning() : Boolean?{
        return timerJob?.isActive
    }

    private fun stopTimer() {
        Log.d("ExerciseTimerService", "Stopping internal timer job.")
        timerJob?.cancel()
        timerJob = null
    }


    override fun onDestroy() {
        Log.d("ExerciseTimerService", "[Hilt] onDestroy called")
        stopTimer()
        timerScope.cancel()
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun sendStopBroadcast() {
        val intent = Intent(BROADCAST_ACTION_STOPPED).apply {
            putExtra(EXTRA_ELAPSED_TIME, currentTime)
        }
        Log.d("ExerciseTimerService", "Sending stop broadcast with time: $currentTime")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
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
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

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
    private fun formatTime(seconds: Int): String {
        return if (seconds < 60) "$seconds s" else "${seconds / 60}m ${seconds % 60}s"
    }
}