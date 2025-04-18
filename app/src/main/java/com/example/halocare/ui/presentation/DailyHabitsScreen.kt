package com.example.halocare.ui.presentation

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.halocare.R
import com.example.halocare.services.ExerciseTimerService
import com.example.halocare.ui.models.ExerciseData
import com.example.halocare.ui.models.JournalEntryData
import com.example.halocare.ui.presentation.charts.HaloCharts
import com.example.halocare.ui.presentation.charts.JournalHeatmap
import com.example.halocare.ui.presentation.charts.ScreenTimePieChart
import com.example.halocare.ui.presentation.charts.SleepTrackerChart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHabitsScreen() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Exercise", "Sleep", "Journaling", "Screen Time")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Habits") },
            )
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
        ) {
            // Tabs
            TabRow(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Chart Placeholder (Replace with actual chart implementation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if(selectedTabIndex == 0){
                    ExerciseTrackerChart()
                }
                if(selectedTabIndex == 1){
                    SleepTrackerChart()
                }
                if(selectedTabIndex == 2){
                    JournalHeatmap()
                }
                if(selectedTabIndex == 3){
                    ScreenTimePieChart()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (selectedTabIndex == 0){
                    ExerciseProgressSection()
                }else if(selectedTabIndex == 1 ){
                    SleepProgressIndicator(sleepHours = 7f, sleepQuality = 2 )
                }else if (selectedTabIndex ==2){
                    JournalStreakProgress()
                }
                else Text("Progress Bar Placeholder")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Log Exercise Progress
            if (selectedTabIndex == 0) { // Exercise Tab
                Text("Start Today's Exercise", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ExerciseTimer()
            }

            // Log Sleep Progress
            if(selectedTabIndex == 1){
                Text(
                    "Sleep Tracking",
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                SleepTracker()
            }

            //Log Journal Progress
            if (selectedTabIndex == 2){
                JournalingTracker()
            }

            //Log Screen time Progress
            if(selectedTabIndex == 3){
                ScreenTimeTracker()
            }
        }
    }
}

@Composable
fun ScreenTimeTracker() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasUsagePermission(context)) }
    var selectedApps by remember { mutableStateOf(mutableSetOf<String>()) }
    var screenTimeData by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }


    val allApps = listOf("YouTube", "Instagram", "Settings", "TikTok", "HaloCare", "Twitter")

    LaunchedEffect(selectedApps) {
        if (hasPermission) {
            screenTimeData = getScreenTimeForApps(context, selectedApps)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        if (!hasPermission) {
            Text("To track screen time, please enable usage access permission.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { openUsageAccessSettings(context) }) {
                Text("Grant Permission")
            }
        } else {
            Text("Select Apps to Track:", fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            allApps.forEach { app ->
                val isSelected = app in selectedApps
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(
                            if (isSelected) Color.Gray else Color.White,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            selectedApps = selectedApps
                                .toMutableSet()
                                .apply {
                                    if (isSelected) remove(app) else add(app)
                                }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(app, fontSize = 16.sp, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Screen Time Data:", fontSize = 18.sp, color = Color.Black)
            screenTimeData.forEach { (app, time) ->
                Text("$app: ${time / 1000 / 60} min", fontSize = 16.sp)
            }

        }
    }
}
fun hasUsagePermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    } else {
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

fun openUsageAccessSettings(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    context.startActivity(intent)
}

fun getInstalledApps(context: Context): Map<String, String> {
    val pm = context.packageManager
    val apps = mutableMapOf<String, String>()

    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val packages = pm.queryIntentActivities(intent, 0)
    for (packageInfo in packages) {
        val appName = packageInfo.loadLabel(pm).toString()
        val packageName = packageInfo.activityInfo.packageName
        apps[appName] = packageName
    }
    return apps
}


fun getScreenTimeForApps(context: Context, selectedApps: Set<String>): Map<String, Long> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - TimeUnit.DAYS.toMillis(1) // Last 24 hours

    val installedApps = getInstalledApps(context) // Get app name → package name map
    val selectedPackages = selectedApps.mapNotNull { installedApps[it] } // Convert names to package names

    val statsMap = mutableMapOf<String, Long>()
    val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

    Log.d("USAGE STATS !1", "getScreenTimeForApps: $usageStatsList")

    if (usageStatsList != null) {
        for (usageStat in usageStatsList) {
            val packageName = usageStat.packageName
            val screenTime = usageStat.totalTimeInForeground

            Log.d("APP USAGE", "Package: ${usageStat.packageName}, Time: ${usageStat.totalTimeInForeground}")

            // Check if this package matches a selected app
            if (selectedPackages.contains(packageName) && screenTime > 0) {
                val appName = installedApps.entries.find { it.value == packageName }?.key ?: packageName
                statsMap[appName] = screenTime
            }
        }
    }
    Log.d("USAGE STATS", "getScreenTimeForApps: $statsMap")

    return statsMap
}

@Preview
@Composable
fun ExerciseTrackerChart(
  //  exerciseDataList: List<ExerciseData>
) {
    val dummyList = listOf(
        ExerciseData(
            exerciseName = "Burpees",
            exerciseDate = "Jan 5",
            timeElapsed = 4500f
        ),
        ExerciseData(
            exerciseName = "Running",
            exerciseDate = "Jan 6",
            timeElapsed = 6000f
        ),
        ExerciseData(
            exerciseName = "Push-ups",
            exerciseDate = "Jan 7",
            timeElapsed = 25000f
        ),
        ExerciseData(
            exerciseName = "Burpees",
            exerciseDate = "Jan 8",
            timeElapsed = 35000f
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondaryContainer
            )
            .padding(16.dp)
    ) {
        Text(text = "Exercise Progress", color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))

        HaloCharts(exerciseDataList = dummyList, featureName = "Exercise Tracker")
    }
}

@Preview()
@Composable
fun ExerciseProgressSection(
    dailyGoal: Int = 50, // in minutes
    currentProgress: Int = 35, // in minutes
    weeklyGoal: Int = 5, // days per week
    daysExercised: Int = 2 // days completed
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Exercise Progress",
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                painter = painterResource(id = R.drawable.dumbell_icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

        // Main Progress Bar - Daily Goal
        LinearProgressIndicator(
            progress = (currentProgress.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.inversePrimary,
            trackColor = Color.LightGray
        )

        Text(
            text = "$currentProgress min / $dailyGoal min",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary Progress Bar - Weekly Streak
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weekly Streak",
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                painter = painterResource(id = R.drawable.baseline_local_fire_department_24),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(weeklyGoal) { index ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (index < daysExercised) MaterialTheme.colorScheme.inversePrimary else Color.LightGray)
                )
            }
        }
    }
}



@Preview()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTracker() {
    var selectedSleepOption by remember { mutableStateOf<String?>(null) }
    var manualSleepHours by remember { mutableStateOf(("")) }
    var sleepQuality by remember  { mutableStateOf<Int?>(2) }
    var loggedToday by remember { mutableStateOf(false) }

    val sleepOptions = listOf("Less than 5 hours", "About 5 hours", "8 hours", "More than 8 hours")
    val sleepQualityIcons = listOf("😴", "🙂", "😐", "😕", "😢")


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("How long did you sleep?", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        sleepOptions.forEach { option ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        if (selectedSleepOption == option) Color.Blue.copy(alpha = 0.3f) else Color.LightGray,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { selectedSleepOption = option },
                contentAlignment = Alignment.Center
            ) {
                Text(option, modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Or enter exact hours:", fontSize = 18.sp)
        TextField(
            value = manualSleepHours,
            onValueChange = { manualSleepHours = it },
            label = { Text("Hours slept") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sleep Quality Rating
        Text("Rate Sleep Quality:", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            sleepQualityIcons.forEachIndexed { index, icon ->
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (sleepQuality == index) Color.Gray else Color.Transparent)
                        .padding(8.dp)
                        .clickable { sleepQuality = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 24.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                loggedToday = true
            },
            enabled = !loggedToday,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (loggedToday) Color.Gray
                else MaterialTheme.colorScheme.primary)
        ) {
            Text(if (loggedToday) "Sleep Data Logged" else "Log Sleep Data")
        }
    }
}

@Composable
fun SleepProgressIndicator(sleepHours: Float, sleepQuality: Int) {
    val targetSleep = 8f // Recommended sleep goal
    val progress = (sleepHours / targetSleep).coerceIn(0f, 1f) // Ensure it's between 0-1

    val progressColor = when {
        sleepHours < 4 -> Color.Red
        sleepHours < 6 -> Color.Yellow
        else -> Color.Green
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Sleep Duration", fontSize = 14.sp, fontWeight = FontWeight.Medium)

        // Circular Progress for Sleep Duration
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(80.dp),
                color = progressColor,
                strokeWidth = 6.dp
            )
            Text(text = "${sleepHours}h", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Sleep Quality", fontSize = 14.sp, fontWeight = FontWeight.Medium)

        // Sleep Quality Segmented Bar with Emoji Label
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(20.dp, 10.dp)
                        .background(if (index < sleepQuality) MaterialTheme.colorScheme.inversePrimary else Color.LightGray)
                        .padding(2.dp)
                )
            }
        }

        // Emoji Label Below Quality Bar
        Text(
            text = when (sleepQuality) {
                5 -> "😴 Excellent Sleep"
                4 -> "🙂 Good Sleep"
                3 -> "😐 Okay Sleep"
                2 -> "😕 Poor Sleep"
                1 -> "😢 Bad Sleep"
                else -> "No Data"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
    }
}

@Composable
fun JournalStreakProgress(
   // entries: List<JournalEntryData>,
    modifier: Modifier = Modifier) {
    val dummyJournals = listOf(
        JournalEntryData(LocalDate.now().minusDays(5), 3),
        JournalEntryData(LocalDate.now().minusDays(4), 2),
        JournalEntryData(LocalDate.now().minusDays(3), 1),
        JournalEntryData(LocalDate.now().minusDays(2), 0),
        JournalEntryData(LocalDate.now().minusDays(1), 6),
        JournalEntryData(LocalDate.now().minusDays(6), 4),
    )
    val today = LocalDate.now()
    val sortedEntries = dummyJournals.sortedByDescending { it.date } // Ensure order
    var currentStreak = 0

    // Calculate streak
    for (i in sortedEntries.indices) {
        val expectedDate = today.minusDays(i.toLong())
        if (sortedEntries[i].date == expectedDate) {
            currentStreak++
        } else break
    }

    val maxStreakGoal = 30 // Set streak goal
    val progress = currentStreak / maxStreakGoal.toFloat()

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Current Streak: $currentStreak🔥", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = Color(0xFFFFA726), // Orange for streak progress
            trackColor = Color.LightGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Goal: $maxStreakGoal Days", style = MaterialTheme.typography.bodySmall)
    }
}


@Preview()
@Composable
fun JournalingTracker() {
    var journalEntry by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Text(
            "Today's Affirmation",
            fontSize = 18.sp,
            color = Color.Gray)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("[Affirmation Placeholder]")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("What are you grateful for today?", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = journalEntry,
            onValueChange = { journalEntry = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Save Journal Entry */ }
        ) {
            Text("Log Journal Entry")
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                painter = painterResource(id = R.drawable.baseline_draw_24),
                contentDescription = null )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseTimer() {
    var time by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var exerciseName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val serviceIntent = Intent(context, ExerciseTimerService::class.java)
            context.startForegroundService(serviceIntent)
        } else {
            Toast.makeText(
                context,
                "Notification permission required for timer",
                Toast.LENGTH_SHORT).show()
        }
    }
    // Register a BroadcastReceiver to listen for stop event
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                isRunning = false
                time = 0 // Reset Timer
            }
        }
        val intentFilter = IntentFilter("EXERCISE_TIMER_STOPPED")
        val flags = Context.RECEIVER_NOT_EXPORTED
        context.registerReceiver(receiver, intentFilter, flags)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = exerciseName,
            onValueChange = { exerciseName = it },
            label = { Text("Enter Exercise Name") },
            singleLine = true,
            readOnly = isRunning,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color.Blue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            val displayTime = if (time < 60) "$time s" else "${time / 60}m ${time % 60}s"

            Text(
                text = displayTime,
                fontSize = 24.sp,
                color = Color.Black
            )
            Icon(
                painter = painterResource(id = R.drawable.baseline_directions_run_24),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 15.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            enabled = exerciseName != "",
            onClick = {
            isRunning = !isRunning
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ){
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }else{
                    toggleTimer(context, isRunning)
                }
            }else {
                toggleTimer(context, isRunning)
            }
            if (isRunning) {
                coroutineScope.launch {
                    while (isRunning) {
                        delay(1000L)
                        time++
                    }
                }
            }
        }) {
            Text(if (isRunning) "Stop" else "Start")
        }
    }
}

fun toggleTimer(context: Context, isRunning: Boolean) {
    if (isRunning) {
        val serviceIntent = Intent(context, ExerciseTimerService::class.java)
        context.startForegroundService(serviceIntent)
    } else {
        val stopIntent = Intent(context, ExerciseTimerService::class.java)
        context.stopService(stopIntent)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDailyHabitsScreen() {
    DailyHabitsScreen()
}
