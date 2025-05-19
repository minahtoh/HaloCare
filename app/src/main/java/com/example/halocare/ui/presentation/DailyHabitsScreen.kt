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
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.halocare.R
import com.example.halocare.services.ExerciseTimerService
import com.example.halocare.ui.models.ExerciseData
import com.example.halocare.ui.models.JournalEntry
import com.example.halocare.ui.models.JournalEntryData
import com.example.halocare.ui.models.ScreenTimeEntry
import com.example.halocare.ui.models.SleepData
import com.example.halocare.ui.presentation.charts.HaloCharts
import com.example.halocare.ui.presentation.charts.JournalHeatmap
import com.example.halocare.ui.presentation.charts.ScreenTimePieChart
import com.example.halocare.ui.presentation.charts.SleepTrackerChart
import com.example.halocare.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHabitsScreen(
    mainViewModel: MainViewModel
) {
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.inversePrimary

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Exercise", "Sleep", "Journaling", "Screen Time")
    var elapsedTime by remember{ mutableStateOf(0f) }
    val context = LocalContext.current
    val time by mainViewModel.currentTime.collectAsState()
    val exerciseName by mainViewModel.exerciseName.collectAsStateWithLifecycle()
    val timerStatus by mainViewModel.isRunning.collectAsStateWithLifecycle()
    val exerciseDataList by mainViewModel.exerciseDataList.collectAsState()
    val sleepDataList by mainViewModel.allSleepData.collectAsState()
    val journalDataList by mainViewModel.allLoggedJournals.collectAsState()
    val today = remember { LocalDate.now().format(DateTimeFormatter.ISO_DATE) }
    val hasLoggedToday = sleepDataList.any { it.dayLogged == today }
    val showJournalDialog = remember { mutableStateOf(false) }
    val selectedJournalEntries = remember { mutableStateOf<List<JournalEntry>>(emptyList()) }
    val screenTimeSummary = remember { mutableStateListOf<ScreenTimeEntry>() }



    LaunchedEffect(Unit){
        statusBarController.updateStatusBar(
            color = statusBarColor,
            darkIcons = true
        )
        mainViewModel.getExerciseDataList()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Daily Habits",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                ) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.inversePrimary)
            )
        },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer
    ) { paddingValues ->
        if (showJournalDialog.value) {
            if (selectedJournalEntries.value.isNotEmpty()){
                JournalViewDialog(
                    journalEntries = selectedJournalEntries.value,
                    onDismiss = { showJournalDialog.value = false }
                )
            }
        }
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(1.dp)
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
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {

                // Chart Placeholder (Replace with actual chart implementation)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if(selectedTabIndex == 0){
                        ExerciseTrackerChart(exerciseDataList ?: emptyList())
                    }
                    if(selectedTabIndex == 1){
                        SleepTrackerChart(sleepDataList)
                    }
                    if(selectedTabIndex == 2){
                        JournalHeatmap(
                            entries = journalDataList,
                            onDateClicked = {
                                    entries ->
                                selectedJournalEntries.value = entries
                                showJournalDialog.value = true
                            }
                        )
                    }
                    if(selectedTabIndex == 3){
                        ScreenTimePieChart(
                            screenTimeSummary.toList()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedTabIndex == 0){
                        ExerciseProgressSection(
                            currentProgress = getTodayExerciseTotal(exerciseDataList ?: emptyList()),
                            exerciseList = exerciseDataList ?: emptyList()
                        )
                    }else if(selectedTabIndex == 1 ){
                        val lastSleepData = sleepDataList.reversed().getOrNull(0)
                        SleepProgressIndicator(
                            sleepHours = lastSleepData?.sleepLength ?: 0f,
                            sleepQuality = lastSleepData?.sleepQuality ?: 1
                        )
                    }else if (selectedTabIndex == 2){
                        JournalStreakProgress(journalDataList)
                    }
                    else Text("Progress Bar Placeholder")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Log Exercise Progress
                if (selectedTabIndex == 0) { // Exercise Tab
                    Text("Start Today's Exercise", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExerciseTimer(
                        onTimerStopped = {
                            mainViewModel.saveExerciseData(it)
                            elapsedTime = it.timeElapsed
                        },
                        time = time,
                        exerciseName = exerciseName,
                        updateExerciseName = {mainViewModel.onExerciseNameChange(it)},
                        clearTimerState = {mainViewModel.clearTimerState()},
                        isTimerRunning = timerStatus
                    )
                }

                // Log Sleep Progress
                if(selectedTabIndex == 1){
                    Text(
                        "Sleep Tracking",
                        fontSize = 24.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if(!hasLoggedToday){
                        SleepTracker(
                            saveSleepData = {
                                mainViewModel.logSleepData(it)
                            }
                        )
                    }else{
                        Box(modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.sleeping_icon_for_dissclaimer),
                                    contentDescription = "sleeping",
                                    modifier = Modifier.size(200.dp),
                                    //colorFilter = ColorFilter.tint(color = Color.Unspecified)
                                )
                                Text(
                                    text = "You’ve already logged your sleep for today.",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                }

                //Log Journal Progress
                if (selectedTabIndex == 2){
                    JournalingTracker(
                        saveJournal = {
                            mainViewModel.saveJournalEntry(it)
                        }
                    )
                }

                //Log Screen time Progress
                if(selectedTabIndex == 3){
                    ScreenTimeTracker(
                        screenTimeSummary = screenTimeSummary,
                        onAppSelected = { entry ->
                            // Update the summary list when an app is selected
                            screenTimeSummary.removeAll { it.appName == entry.appName }
                            if (entry.minutes > 0) {
                                screenTimeSummary.add(entry)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenTimeTracker(
    screenTimeSummary: MutableList<ScreenTimeEntry>,
    onAppSelected : (ScreenTimeEntry)-> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasUsagePermission(context)) }
    var selectedApps by remember { mutableStateOf(mutableSetOf<String>()) }
    var screenTimeData by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }


    val allApps = listOf("YouTube", "Instagram", "Settings", "TikTok", "HaloCare", "Twitter")
    val defaultColorPalette = listOf(
        Color(0xFFE57373), // red
        Color(0xFF64B5F6), // blue
        Color(0xFF81C784), // green
        Color(0xFFFFD54F), // yellow
        Color(0xFFBA68C8), // purple
        Color(0xFFFF8A65)  // orange
    )

    val appColorMap = remember {
        allApps.mapIndexed { index, app ->
            app to defaultColorPalette[index % defaultColorPalette.size]
        }.toMap()
    }

    LaunchedEffect(selectedApps) {
        if (hasPermission) {
            val data = getScreenTimeForApps(context, selectedApps)
            screenTimeData = data

            // Send updated data to parent
            data.forEach { (app, time) ->
                onAppSelected(
                    ScreenTimeEntry(
                        appName = app,
                        minutes = (time / 1000 / 60).toInt(),
                        color = appColorMap[app] ?: Color.Gray
                    )
                )
            }
            val removedApps = screenTimeSummary.map { it.appName } - data.keys
            removedApps.forEach { removedApp ->
                onAppSelected(
                    ScreenTimeEntry(
                        appName = removedApp,
                        minutes = 0,
                        color = appColorMap[removedApp] ?: Color.Gray
                    )
                )
            }

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

@Composable
fun ExerciseTrackerChart(
    exerciseDataList: List<ExerciseData>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .shadow(elevation = 7.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(15.dp)
            )
            .padding(6.dp)
    ) {
        Text(text = "Exercise Progress", color = Color.Black, modifier = Modifier.padding(10.dp))
        Spacer(modifier = Modifier.height(8.dp))

        HaloCharts(exerciseDataList = exerciseDataList.takeLast(20), featureName = "Exercise Tracker")
    }
}

fun getTodayExerciseTotal(exerciseList: List<ExerciseData>): Int {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    return exerciseList
        .filter { it.exerciseDate == today }
        .sumOf { (it.timeElapsed / 60f).roundToInt() }
}

//@Preview()
@Composable
fun ExerciseProgressSection(
    dailyGoal: Int = 50, // in minutes
    currentProgress: Int = 35, // in minutes
    weeklyGoal: Int = 5, // days per week
    daysExercised: Int = 2, // days completed
    exerciseList: List<ExerciseData>
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
        ) {
            WeeklyStreakRow(completedDates = getCompletedExerciseDates(exerciseList,50))
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🔥 Longest streak: ${getLongestStreak(getCompletedExerciseDates(exerciseList, 50))} days",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
@Composable
fun WeeklyStreakRow(
    completedDates: Set<LocalDate>,
    weeklyGoal: Int = 7,
    dailyGoal: Int = 50
) {
    val today = LocalDate.now()
    val startOfWeek = today.with(DayOfWeek.MONDAY)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until weeklyGoal) {
            val day = startOfWeek.plusDays(i.toLong())
            val isCompleted = completedDates.contains(day)

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted)
                            MaterialTheme.colorScheme.inversePrimary
                        else
                            Color.LightGray
                    )
            )
        }
    }
}

fun getLongestStreak(completedDates: Set<LocalDate>): Int {
    val sortedDates = completedDates.sorted()
    var longest = 0
    var current = 0
    var prevDate: LocalDate? = null

    for (date in sortedDates) {
        if (prevDate == null || prevDate.plusDays(1) == date) {
            current++
        } else {
            current = 1
        }
        longest = maxOf(longest, current)
        prevDate = date
    }

    return longest
}

fun convertFloatTimeToMinutes(floatTime: Float): Int {
    val hours = floatTime.toInt()
    val minutes = ((floatTime - hours) * 60).roundToInt()
    return (hours * 60) + minutes
}

fun getCompletedExerciseDates(
    exerciseList: List<ExerciseData>,
    dailyGoalInMinutes: Int
): Set<LocalDate> {
    return exerciseList
        .groupBy { LocalDate.parse(it.exerciseDate) }
        .filter { (_, entries) ->
            val totalMinutes = entries.sumOf { convertFloatTimeToMinutes(it.timeElapsed) }
            totalMinutes >= dailyGoalInMinutes
        }
        .keys
}




//@Preview()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTracker(
    saveSleepData : (SleepData) -> Unit
) {
    var selectedSleepOption by remember { mutableStateOf<String?>(null) }
    var manualSleepHours by remember { mutableStateOf(("")) }
    var sleepQuality by remember  { mutableStateOf(2) }
    var loggedToday by remember { mutableStateOf(false) }
    var showSlider by remember { mutableStateOf(true) }
    var manualSleepSliderValue by remember { mutableStateOf(6f) } // Default 6 hours


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
            value =  "${manualSleepSliderValue.roundToInt()} hours",
            onValueChange = { manualSleepHours = it },
            label = { Text("Hours slept") },
            readOnly = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showSlider = !showSlider
                    Log.d("ONTEXTFIELDCLICK", "SleepTracker: showslider$showSlider")
                }
                .padding(20.dp)
                ,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        )
        if (showSlider){
            selectedSleepOption = null
            AnimatedVisibility(
                visible = showSlider,
                enter = expandVertically(animationSpec = tween(durationMillis = 400)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Sleep Duration",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Slider(
                        value = manualSleepSliderValue,
                        onValueChange = { manualSleepSliderValue = it },
                        valueRange = 0f..24f,
                        steps = 23, // gives whole hour ticks
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Selected: ${String.format("%.1f", manualSleepSliderValue)} hours",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

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
                val sleepData = SleepData(
                    dayLogged = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                    sleepQuality = sleepQuality,
                    sleepLength = getSleepLengthInHours(selectedSleepOption,manualSleepSliderValue,showSlider)
                )
                saveSleepData(sleepData)
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

fun getSleepLengthInHours(
    selectedOption: String?,
    sliderInput: Float,
    isCustomInputUsed: Boolean
): Float {
    return if (isCustomInputUsed) {
        sliderInput
    } else {
        when (selectedOption) {
            "Less than 5 hours" -> 4f
            "About 5 hours" -> 5f
            "8 hours" -> 8f
            "More than 8 hours" -> 9f
            else -> 0f // fallback
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
    entries: List<JournalEntry>,
    modifier: Modifier = Modifier
) {

    val today = LocalDate.now()
    val uniqueDates = entries.map { it.date }.toSet()
    var currentStreak = 0

    for (i in 0..uniqueDates.size) {
        val expectedDate = today.minusDays(i.toLong())
        if (expectedDate in uniqueDates) {
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
fun JournalingTracker(
    saveJournal : (JournalEntry) -> Unit = {}
) {
    var journalEntry by remember { mutableStateOf("") }
    var showLoadingDialog by remember{ mutableStateOf(false) }
    var selectedJournalType by remember { mutableStateOf("scroll") }
    val maxCharacters = when (selectedJournalType) {
        "scroll" -> 150
        "notebook" -> 300
        "sticky_note" -> 100
        "open_book" -> 200
        else -> 0
    }
    val isOverLimit = journalEntry.length > maxCharacters
    if (showLoadingDialog){
        JournalSaveDialog {
            journalEntry = ""
            showLoadingDialog = false
        }
    }
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
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(4.dp)
        ) {
            JournalInputWithLimit(
                journalEntry = journalEntry,
                onJournalChange = { journalEntry = it },
                maxCharacters = maxCharacters
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            JournalTypeSelector(
                selectedType = selectedJournalType,
                onTypeSelected = { selectedJournalType = it }
            )

            Button(
                onClick = {
                    val journal = JournalEntry(
                        date = LocalDate.now(),
                        entryText = journalEntry,
                        journalType = selectedJournalType
                    )
                    saveJournal(journal)
                    showLoadingDialog = true
                },
                enabled = journalEntry.isNotBlank() && !isOverLimit
            ) {
                Text("Log Entry")
                Spacer(modifier = Modifier.width(5.dp))
                Icon(
                    painter = painterResource(id = R.drawable.baseline_draw_24),
                    contentDescription = null
                )
            }
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun JournalInputWithLimit(
    journalEntry: String,
    onJournalChange: (String) -> Unit,
    maxCharacters: Int = 150
) {
    val annotatedText = buildAnnotatedString {
        val normalText = journalEntry.take(maxCharacters)
        val overflowText = journalEntry.drop(maxCharacters)

        append(normalText)
        if (overflowText.isNotEmpty()) {
            withStyle(SpanStyle(color = Color.Red)) {
                append(overflowText)
            }
        }
    }

    Column {
        BasicTextField(
            value = journalEntry,
            onValueChange = onJournalChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxSize()) {
                    if (journalEntry.isEmpty()) {
                        Text(
                            text = "Enter your journal...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            },
            visualTransformation = {
                TransformedText(annotatedText, OffsetMapping.Identity)
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            val overLimit = journalEntry.length > maxCharacters
            Text(
                text = "${journalEntry.length}/$maxCharacters",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (overLimit) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun JournalTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        JournalTypeIcon(
            icon = R.drawable.scroll_or_parchment_typ,
            type = "scroll",
            selectedType = selectedType,
            onTypeSelected = onTypeSelected
        )
        JournalTypeIcon(
            icon = R.drawable.sheet_of_paper_ic_typ,
            type = "notebook",
            selectedType = selectedType,
            onTypeSelected = onTypeSelected
        )
        JournalTypeIcon(
            icon =  R.drawable.sticky_note_ic,
            type = "sticky_note",
            selectedType = selectedType,
            onTypeSelected = onTypeSelected
        )
        JournalTypeIcon(
            icon = R.drawable.open_journal_book_1_ic_typ,
            type = "open_book",
            selectedType = selectedType,
            onTypeSelected = onTypeSelected
        )
    }
}

@Composable
fun JournalTypeIcon(
    @DrawableRes icon: Int,
    type: String,
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    IconButton(
        onClick = { onTypeSelected(type) }
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = type,
            tint = if (selectedType == type) MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.9f) else Color.Unspecified,
            modifier = Modifier.size(40.dp)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun JournalViewDialog(
    journalEntries: List<JournalEntry>, // List of journal entries
    onDismiss: () -> Unit
) {
    val currentEntryIndex = remember { mutableStateOf(0) }

    // Check if there are multiple entries
    val hasMultipleEntries = journalEntries.size > 1


    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(min = 600.dp)
                .padding(16.dp)
        ) {

            var previousIndex by remember { mutableStateOf(0) }
            val transitionSpec: AnimatedContentTransitionScope<Int>.() -> ContentTransform = {
                if (targetState > initialState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            }


            AnimatedContent(
                targetState = currentEntryIndex.value,
                transitionSpec = transitionSpec,
                modifier = Modifier.fillMaxSize()
            ) { index ->

                val currentJournal = journalEntries[index]

                val journalBackground = when (currentJournal.journalType.lowercase()) {
                    "scroll" -> painterResource(id = R.drawable.scroll_bg)
                    "notebook" -> painterResource(id = R.drawable.paper_bg)
                    "sticky_note" -> painterResource(id = R.drawable.sticky_note_bg)
                    "open_book" -> painterResource(id = R.drawable.open_journal_am_bg_f)
                    else -> painterResource(id = R.drawable.journal_scroll_bg)
                }

                val journalFont = when (currentJournal.journalType.lowercase()) {
                    "scroll" -> FontFamily(Font(R.font.parisienne))
                    "notebook" -> FontFamily(Font(R.font.patrick_hand))
                    "sticky_note" -> FontFamily(Font(R.font.architects_daughter))
                    "open_book" -> FontFamily(Font(R.font.satisfy))
                    else -> FontFamily(Font(R.font.la_belle_aurore))
                }

                val backgroundPaddingTop = when (currentJournal.journalType.lowercase()) {
                    "scroll" -> 150.dp
                    "notebook" -> 120.dp
                    "sticky_note" -> 160.dp
                    "open_book" -> 195.dp
                    else -> 0.dp
                }

                val backgroundPaddingStart = when (currentJournal.journalType.lowercase()) {
                    "notebook" -> 20.dp
                    "scroll" -> 5.dp
                    else -> 0.dp
                }

                val backgroundPaddingEnd = when (currentJournal.journalType.lowercase()) {
                    "open_book" -> 0.dp
                    else -> 0.dp
                }

                val textColor = when (currentJournal.journalType.lowercase()) {
                    "notebook" -> Color.Blue.copy(alpha = 0.5f)
                    "scroll" -> Color.DarkGray.copy(alpha = 0.7f)
                    else -> Color.DarkGray
                }

                val fontSize = when (currentJournal.journalType.lowercase()) {
                    "notebook" -> 15.sp
                    "open_book" -> 13.sp
                    else -> 17.sp
                }

                val lineHeight = when (currentJournal.journalType.lowercase()) {
                    "notebook" -> 15.sp
                    "open_book" -> 15.sp
                    else -> 25.sp
                }

                val dateTextPadding = when (currentJournal.journalType.lowercase()) {
                    "scroll" -> 130.dp
                    "notebook" -> 85.dp
                    "sticky_note" -> 160.dp
                    else -> 0.dp
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Background
                    Image(
                        painter = journalBackground,
                        contentDescription = "Journal Background",
                        modifier = Modifier
                            .height(600.dp)
                            .width(450.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )

                    // Foreground Text
                    Box(
                        modifier = Modifier
                            .height(600.dp)
                            .width(450.dp)
                            .padding(32.dp)
                            .padding(
                                top = backgroundPaddingTop,
                                start = backgroundPaddingStart,
                                end = backgroundPaddingEnd
                            )
                    ) {
                        if (currentJournal.journalType == "open_book"){
                            TwoColumnNotebookText(
                                fullText = currentJournal.entryText,
                                date = currentJournal.date.toString() + ".",
                                font = journalFont,
                                maxCharsPerColumn = 100
                            )
                        } else{
                            Text(
                                text = currentJournal.entryText,
                                style = TextStyle(
                                    fontFamily = journalFont,
                                    fontSize = fontSize,
                                    lineHeight = lineHeight,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            )
                            Text(
                                text = "${currentJournal.date}.",
                                style = TextStyle(
                                    fontFamily = journalFont,
                                    fontSize = fontSize,
                                    lineHeight = lineHeight,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = dateTextPadding, end = 5.dp)
                            )
                        }
                    }
                }
            }


            IconButton(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .size(40.dp)
                    .align(Alignment.TopEnd),
                onClick = {
                    onDismiss()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "close",
                    tint = MaterialTheme.colorScheme.onError,

                    )
            }

            if (hasMultipleEntries) {
                // Previous button (only visible when NOT on first item)
                AnimatedVisibility(
                    visible = currentEntryIndex.value > 0,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut() + slideOutHorizontally(),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 26.dp)
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                currentEntryIndex.value = currentEntryIndex.value - 1
                                previousIndex = currentEntryIndex.value

                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Previous Entry",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Next button (only visible when NOT on last item)
                AnimatedVisibility(
                    visible = currentEntryIndex.value < journalEntries.size - 1,
                    enter = fadeIn() + slideInHorizontally { it / 2 },
                    exit = fadeOut() + slideOutHorizontally { it / 2 },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 26.dp)
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                currentEntryIndex.value = currentEntryIndex.value + 1
                                previousIndex = currentEntryIndex.value
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Next Entry",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun TwoColumnNotebookText(
    fullText: String,
    date: String,
    modifier: Modifier = Modifier,
    font: FontFamily,
    textStyle: TextStyle = TextStyle.Default,
    maxCharsPerColumn: Int = 1000 // adjust as needed
) {
    // Split the text manually
    val midpoint = fullText.length.coerceAtMost(maxCharsPerColumn)
    val firstHalf = fullText.substring(0, midpoint)
    val secondHalf = fullText.substring(midpoint)

    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$date\n$firstHalf",
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            style = textStyle.copy(fontFamily = font, fontSize = 15.sp, fontWeight = FontWeight.Bold),
            lineHeight = 16.sp
        )
        Text(
            text = secondHalf,
            modifier = Modifier
                .weight(1f)
                .padding(start = 18.dp),
            style = textStyle.copy(fontFamily = font, fontSize = 15.sp, fontWeight = FontWeight.Bold),
            lineHeight = 16.sp
        )
    }
}

@Composable
fun JournalSaveDialog(
    onFinished: () -> Unit
) {
    var isSaving by remember { mutableStateOf(true) }

    // Automatically transition after 2 seconds
    LaunchedEffect(Unit) {
        delay(2000)
        isSaving = false
        delay(1000) // Show checkmark for 1 second
        onFinished()
    }

    Dialog(onDismissRequest = { /* Block manual dismiss */ }) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(200.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Writing...", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Saved",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Journal Saved!", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseTimer(
    onTimerStopped : (ExerciseData) -> Unit,
    time : Int,
    exerciseName: String,
    updateExerciseName : (String) -> Unit,
    clearTimerState: () -> Unit,
    isTimerRunning: Boolean
) {

    var isRunning by remember { mutableStateOf(isTimerRunning) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current



    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startTimerService(context)
            isRunning = true
        } else {
            // Permission denied
            Toast.makeText(context, "Notification permission is required to show timer progress", Toast.LENGTH_LONG).show()
            isRunning = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "highlightRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )
    val highlightAlpha by animateFloatAsState(
        targetValue = if (isRunning) 1f else 0f, // Target 1f if running, 0f otherwise
        animationSpec = tween(durationMillis = 300),
        label = "highlightAlpha"
    )
    val highlightColor = MaterialTheme.colorScheme.inversePrimary


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = exerciseName,
            onValueChange = { updateExerciseName(it) },
            label = { Text("Enter Exercise Name") },
            singleLine = true,
            readOnly = isRunning,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(150.dp)
                .drawBehind {
                    if (highlightAlpha > 0f) {
                        val strokeWidthPx = 8.dp.toPx()
                        rotate(rotationAngle) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        highlightColor.copy(alpha = 0.1f), // Use base highlight color
                                        highlightColor,
                                        highlightColor.copy(alpha = 0.1f),
                                        Color.Transparent
                                    ),
                                    center = center
                                ),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                                size = Size(
                                    size.width - strokeWidthPx,
                                    size.height - strokeWidthPx
                                ),
                                style = Stroke(width = strokeWidthPx),
                                alpha = highlightAlpha
                            )
                        }
                    }
                }
                .clip(CircleShape)
                .background(Color.Blue.copy(alpha = 0.2f))
            ,
            contentAlignment = Alignment.Center
        ) {
            val displayTime = if (time < 60) "$time s" else "${time / 60}m ${time % 60}s"

            Text(
                text = displayTime,
                fontSize = 24.sp,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
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

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier.align(Alignment.Center),
                    enabled = exerciseName.isNotBlank(),
                    onClick = {
                        if (isRunning) {
                            stopTimerService(context)
                            isRunning = false
                        } else {

                            // Check for Notification Permission on Android 13+
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                    != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    startTimerService(context)
                                    isRunning = true
                                }
                            } else {
                                startTimerService(context)
                                isRunning = true
                            }
                        }
                    }
                ) {
                    Text(if (isRunning) "Stop" else "Start")
                }
                if (!isRunning && time != 0){
                    Button(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onClick = {
                            val loggedExercise = ExerciseData(
                                exerciseName = exerciseName,
                                timeElapsed = time.toFloat(),
                                exerciseDate = LocalDate.now().toString()
                            )
                            onTimerStopped(loggedExercise)
                            clearTimerState()
                        }
                    ) {
                        Text(text = "Log Time")
                    }
                }
            }
        }
    }
}



private fun startTimerService(context: Context) {
    Log.d("ExerciseTimer", "Requesting Service Start")
    val serviceIntent = Intent(context, ExerciseTimerService::class.java)
    ContextCompat.startForegroundService(context, serviceIntent)
}

private fun stopTimerService(context: Context) {
    val serviceIntent = Intent(context, ExerciseTimerService::class.java).apply {
        action = ExerciseTimerService.ACTION_STOP_SERVICE
    }
    context.startService(serviceIntent)
}


@Preview(showBackground = true)
@Composable
fun PreviewDailyHabitsScreen() {
 //   DailyHabitsScreen()
}
