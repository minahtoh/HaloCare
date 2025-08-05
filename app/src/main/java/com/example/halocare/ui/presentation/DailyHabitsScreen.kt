package com.example.halocare.ui.presentation

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.graphics.Shader
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ComponentRegistry
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.halocare.R
import com.example.halocare.services.ExerciseTimerService
import com.example.halocare.ui.models.ExerciseData
import com.example.halocare.ui.models.JournalEntry
import com.example.halocare.ui.models.ScreenTimeEntry
import com.example.halocare.ui.models.SleepData
import com.example.halocare.ui.presentation.charts.AnimatedText
import com.example.halocare.ui.presentation.charts.HaloCharts
import com.example.halocare.ui.presentation.charts.JournalHeatmap
import com.example.halocare.ui.presentation.charts.NotebookBackground
import com.example.halocare.ui.presentation.charts.NotebookJournalView
import com.example.halocare.ui.presentation.charts.OpenBookBackground
import com.example.halocare.ui.presentation.charts.OpenBookJournalView
import com.example.halocare.ui.presentation.charts.ScreenTimePieChart
import com.example.halocare.ui.presentation.charts.ScrollBackground
import com.example.halocare.ui.presentation.charts.ScrollJournalView
import com.example.halocare.ui.presentation.charts.SleepTrackerChart
import com.example.halocare.ui.presentation.charts.StickyNoteBackground
import com.example.halocare.ui.presentation.charts.StickyNoteJournalView
import com.example.halocare.ui.presentation.charts.formatTextWithLineBreaks
import com.example.halocare.ui.presentation.charts.toJournalType
import com.example.halocare.ui.utils.ConfirmActionDialog
import com.example.halocare.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHabitsScreen(
    mainViewModel: MainViewModel,
    onBackIconClick : () -> Unit
) {
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.inversePrimary

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Exercise", "Sleep", "Journaling", "Screen Time")
    val exerciseDataList by mainViewModel.exerciseDataList.collectAsState()
    val sleepDataList by mainViewModel.allSleepData.collectAsState()
    val journalDataList by mainViewModel.allLoggedJournals.collectAsState()
    val today = remember { LocalDate.now().format(DateTimeFormatter.ISO_DATE) }
    val showJournalDialog = remember { mutableStateOf(false) }
    val selectedJournalEntries = remember { mutableStateOf<List<JournalEntry>>(emptyList()) }
    val screenTimeSummary = remember { mutableStateListOf<ScreenTimeEntry>() }
    val focusManager = LocalFocusManager.current
    // Pager state for swipe gestures
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val scrollState = rememberScrollState()
    val topAppBarHeight = 75.dp

    val isTopBarVisible by remember { derivedStateOf { scrollState.value < 100 } }

    val offsetY by animateDpAsState(
        targetValue = if (isTopBarVisible) 0.dp else -topAppBarHeight,
        animationSpec = tween(durationMillis = 300),
        label = "TopBarPull"
    )
    val animatedTopPadding by animateDpAsState(
        targetValue = if (isTopBarVisible) 50.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    val containerHeight by animateDpAsState(
        targetValue = if (isTopBarVisible) 75.dp  else 50.dp,
        animationSpec = tween(durationMillis = 300)
    )


    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }
    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }


    LaunchedEffect(Unit){
        statusBarController.updateStatusBar(
            color = statusBarColor,
            darkIcons = true
        )
        mainViewModel.getExerciseDataList()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) { paddingValues ->
        if (showJournalDialog.value && selectedJournalEntries.value.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                JournalViewDialog(
                    journalEntries = selectedJournalEntries.value,
                    onDismiss = { showJournalDialog.value = false }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val maxContainerHeight = topAppBarHeight + 50.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(containerHeight), // containerHeight = topBar + tab height when visible
                contentAlignment = Alignment.TopCenter
            ) {
                DailyHabitsTopBar(
                    onBackIconClick = { onBackIconClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(topAppBarHeight)
                        .offset { IntOffset(x = 0, y = offsetY.roundToPx()) } // replaces graphicsLayer
                )

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .offset { IntOffset(x = 0, y = (offsetY + topAppBarHeight).roundToPx()) }, // properly placed below top bar
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }

            // Main Content BELOW header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = animatedTopPadding,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = paddingValues.calculateBottomPadding()
                    )
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(rememberNestedScrollConnection())
                ) { page ->
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .fillMaxSize()
                            .padding(0.dp)
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    focusManager.clearFocus()
                                }
                            }
                    ) {
                        when (page) {
                            0 -> ExerciseTabContent(
                                mainViewModel = mainViewModel,
                                exerciseDataList = exerciseDataList
                            )

                            1 -> SleepTabContent(
                                sleepDataList = sleepDataList,
                                mainViewModel = mainViewModel
                            )

                            2 -> JournalTabContent(
                                mainViewModel = mainViewModel,
                                journalDataList = journalDataList,
                                onDatePressed = {
                                    selectedJournalEntries.value = it
                                    showJournalDialog.value = true
                                }
                            )

                            3 -> ScreenTimeTabContent(screenTimeSummary = screenTimeSummary)
                        }
                        Spacer(modifier = Modifier.height(7.dp))
                    }
                }
            }
        }
    }
}

private fun rememberNestedScrollConnection() = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        // Handle nested scrolling if needed
        return Offset.Zero
    }
}

@Composable
private fun ExerciseTabContent(
    exerciseDataList: List<ExerciseData>?,
    mainViewModel: MainViewModel
){
    var elapsedTime by remember{ mutableStateOf(0f) }
    val time by mainViewModel.currentTime.collectAsState()
    val exerciseName by mainViewModel.exerciseName.collectAsStateWithLifecycle()
    val timerStatus by mainViewModel.isRunning.collectAsStateWithLifecycle()

    // Chart Placeholder
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(305.dp) // slightly more than chart height
            .padding(horizontal = 1.dp)
    ) {
        // Folder background layer (static shape)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .height(300.dp)
                .padding(horizontal = 10.dp)
                .shadow(3.dp, RoundedCornerShape(14.dp), clip = false)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
        )

        // Main card/chart
        ExerciseTrackerChart(exerciseDataList ?: emptyList())
    }



    // Progress Bar Placeholder
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(385.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        //contentAlignment = Alignment.Center
    ){
        ExerciseProgressSection(
            currentProgress = getTodayExerciseTotal(exerciseDataList ?: emptyList()),
            exerciseList = exerciseDataList ?: emptyList()
        )
    }
    Spacer(modifier = Modifier.height(3.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp)
            ) {
                Text(
                    "Start Today's Exercise",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExerciseTimer(
                    onTimerStopped = {
                        mainViewModel.saveExerciseData(it)
                        elapsedTime = it.timeElapsed
                    },
                    time = time,
                    exerciseName = exerciseName,
                    updateExerciseName = { mainViewModel.onExerciseNameChange(it) },
                    clearTimerState = { mainViewModel.clearTimerState() },
                    isTimerRunning = timerStatus
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
    Spacer(modifier = Modifier.height(300.dp))
}

@Composable
private fun SleepTabContent(
    sleepDataList: List<SleepData>,
    mainViewModel: MainViewModel
){
    val today = remember { LocalDate.now().format(DateTimeFormatter.ISO_DATE) }
    val hasLoggedToday = sleepDataList.any { it.dayLogged == today }

    // Chart Placeholder
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(305.dp) // slightly more than chart height
            .padding(horizontal = 6.dp)
    ) {
        // Folder background layer (static shape)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .height(300.dp)
                .padding(horizontal = 10.dp)
                .shadow(3.dp, RoundedCornerShape(14.dp), clip = false)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
        )

        // Main card/chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .shadow(7.dp, RoundedCornerShape(15.dp), clip = false)
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Sleep Pattern",
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                SleepTrackerChart(sleepDataList)
            }
        }
    }
    Spacer(modifier = Modifier.height(7.dp))
    // Progress Bar Placeholder
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        //contentAlignment = Alignment.Center
    ){
        val lastSleepData = sleepDataList.getOrNull(0)
        if (!hasLoggedToday){
            SleepAnalysisPlaceHolder(sleepDataList = sleepDataList)
        } else{
            SleepProgressIndicator(
                sleepHours = lastSleepData?.sleepLength ?: 0f,
                sleepQuality = lastSleepData?.sleepQuality ?: 1
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(700.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
            )
            .padding(3.dp)
    ){
        Box(modifier = Modifier.fillMaxSize()){

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {

                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text ="Log Today Sleep",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if(!hasLoggedToday){
                    SleepTracker(
                        saveSleepData = {
                            mainViewModel.logSleepData(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(190.dp))
                }else{
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
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
                            Spacer(modifier = Modifier.height(190.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalTabContent(
    journalDataList: List<JournalEntry>,
    mainViewModel: MainViewModel,
    onDatePressed: (List<JournalEntry>) -> Unit
) {

    // Chart Placeholder
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp) // slightly more than chart height
            .padding(3.dp)
    ) {
        // Folder background layer (static shape)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .height(300.dp)
                .padding(horizontal = 10.dp)
                .shadow(3.dp, RoundedCornerShape(14.dp), clip = false)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.BottomCenter)
                .shadow(7.dp, RoundedCornerShape(15.dp), clip = false)
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(bottom = 2.dp)
        ){
            // Main card/chart
            JournalHeatmap(
                entries = journalDataList,
                onDateClicked = { entries ->
                    onDatePressed(entries)
                }
            )
        }

    }

    // Progress Bar Placeholder
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        //contentAlignment = Alignment.Center
    ) {
        JournalStreakProgress(journalDataList)
    }

    // Affirmation Card
    Card(
        modifier = Modifier.fillMaxWidth().padding(7.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today's Affirmation",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = getDailyAffirmation(), // Replace with your affirmation logic
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        JournalingTracker(
            saveJournal = {
                mainViewModel.saveJournalEntry(it)
            }
        )
    }
}

@Composable
private fun ScreenTimeTabContent(
    screenTimeSummary: MutableList<ScreenTimeEntry>
){
    // Chart Placeholder
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(305.dp) // slightly more than chart height
            .padding(horizontal = 6.dp)
    ) {
        // Folder background layer (static shape)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .height(300.dp)
                .padding(horizontal = 10.dp)
                .shadow(3.dp, RoundedCornerShape(14.dp), clip = false)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
        )

        // Main card/chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .shadow(7.dp, RoundedCornerShape(15.dp), clip = false)
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Screen Time Usage",
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                ScreenTimePieChart(
                    screenTimeSummary.toList()
                )
            }
        }
    }


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
@Composable
fun ScreenTimeTracker(
    screenTimeSummary: MutableList<ScreenTimeEntry>,
    onAppSelected : (ScreenTimeEntry)-> Unit,
) {
    val context = LocalContext.current
    val hasPermission by remember { mutableStateOf(hasUsagePermission(context)) }
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
    Box(
        modifier = Modifier
            .padding(6.dp) // space around the shadow
            .shadow(
                elevation = 7.dp,
                shape = RoundedCornerShape(15.dp),
                clip = false
            )
            .clip(RoundedCornerShape(15.dp)) // actually clips the content
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(12.dp) // internal padding
        ) {
            Text(
                text = "Exercise Progress",
                color = Color.Black,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            HaloCharts(
                exerciseDataList = exerciseDataList.takeLast(20),
                featureName = "Exercise Tracker"
            )
        }
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
    exerciseList: List<ExerciseData>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
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
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
            )
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(9.dp), clip = true)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.dumbell_icon),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(13.dp))
                .background(
                    shape = RoundedCornerShape(13.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 25.dp),
            verticalArrangement = Arrangement.Center
        ) {
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

        }
        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = "$currentProgress min / $dailyGoal min",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Secondary Progress Bar - Weekly Streak
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weekly Streak",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
            )
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(9.dp))
                    .background(color = MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_local_fire_department_24),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
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
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until weeklyGoal) {
            val day = startOfWeek.plusDays(i.toLong())
            val isCompleted = completedDates.contains(day)
            val dayLabel = day.dayOfWeek.name.take(3).capitalize()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Day label
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Pill indicator
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(18.dp)
                        .shadow(
                            elevation = if (isCompleted) 2.dp else 1.dp,
                            shape = RoundedCornerShape(50)
                        )
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isCompleted)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                )
            }
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

@Composable
fun SleepAnalysisPlaceHolder(
    sleepDataList : List<SleepData>
){
    Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Sleep Duration Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.elevatedCardElevation(3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Previous Night's Sleep",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%.1f hours".format(sleepDataList.last().sleepLength),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. Quality Visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Quality Rating
                Card(
                    modifier = Modifier
                        .width(175.dp)
                        .height(125.dp)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.elevatedCardElevation(1.dp),
                    shape = RoundedCornerShape(7.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "QUALITY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${sleepDataList.last().sleepQuality}/5",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = sleepDataList.last().sleepQuality / 5f,
                            modifier = Modifier
                                .width(80.dp)
                                .height(8.dp)
                                .padding(top = 4.dp),
                            color = when (sleepDataList.last().sleepQuality) {
                                4, 5 -> Color(0xFF4CAF50)
                                3 -> Color(0xFFFFC107)
                                else -> Color(0xFFF44336)
                            }
                        )
                    }
                }

                // Divider
                Divider(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .height(100.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // Comparison to Average
                Card(
                    modifier = Modifier
                        .width(175.dp)
                        .height(125.dp)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.elevatedCardElevation(1.dp),
                    shape = RoundedCornerShape(7.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "VS YOUR AVERAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        val avgSleep = sleepDataList.map { it.sleepLength }.average().toFloat()
                        val diff = sleepDataList.last().sleepLength - avgSleep
                        Text(
                            text = "${if (diff >= 0) "+" else ""}${"%.1f".format(diff)}h",
                            style = MaterialTheme.typography.titleLarge,
                            color = when {
                                diff > 0.5 -> Color(0xFF4CAF50)
                                diff < -0.5 -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 3. Actionable Tip
            Text(
                text = when (sleepDataList.last().sleepQuality) {
                    5 -> "🌟 Great sleep! Try to maintain this routine."
                    4 -> "☀️ Solid rest. A consistent bedtime could help reach 5/5."
                    3 -> "🌙 Average night. Limit caffeine after 2PM tomorrow."
                    else -> "💤 Rough night. Consider a wind-down routine before bed."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
}


//@Preview()
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalLayoutApi::class)
@Composable
fun SleepTracker(
    saveSleepData: (SleepData) -> Unit
) {
    var selectedSleepOption by remember { mutableStateOf<String?>(null) }
    var sleepQuality by remember { mutableStateOf(2) }
    var loggedToday by remember { mutableStateOf(false) }
    var showSlider by remember { mutableStateOf(false) }
    var manualSleepSliderValue by remember { mutableStateOf(6f) }
    var showSavingDialog by remember { mutableStateOf(false) }

    val sleepOptions = listOf("Less than 5 hours", "About 5 hours", "More than 8 hours", "About 8 hours")
    val sleepQualityIcons = listOf("😢", "😕", "😐", "🙂", "😴")

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ConfirmActionDialog(
            title = "Sleep Tracker",
            message = "Save previous night sleep data?",
            confirmButtonText = "Save",
            onConfirm = {
                showSavingDialog = true
                showDialog = false
            },
            onDismiss = { showDialog = false },
            icon = Icons.Default.CheckCircle)
    }

    if (showSavingDialog){
        AnimatedSaveDialog(
            loadingText = "Logging Sleep Data",
            successText = "Sleep Data Saved"
        ) {
            val sleepData = SleepData(
                dayLogged = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                sleepQuality = sleepQuality,
                sleepLength = getSleepLengthInHours(selectedSleepOption, manualSleepSliderValue, showSlider)
            )
            saveSleepData(sleepData)
            showSavingDialog = false
            loggedToday = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 7.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(7.dp))
            .clip(RoundedCornerShape(7.dp))
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("How long did you sleep?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2
        ) {
            sleepOptions.forEach { option ->
                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    FilterChip(
                        selected = selectedSleepOption == option && !showSlider,
                        onClick = {
                            showSlider = false
                            selectedSleepOption = option
                        },
                        label = { Text(option, fontSize = 14.sp) },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        elevation = FilterChipDefaults.elevatedFilterChipElevation(
                            pressedElevation = 2.dp
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }



        Spacer(Modifier.height(20.dp))

        Text("Or use slider for precise input:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Card(
            onClick = {
                selectedSleepOption = null
                showSlider = !showSlider
            },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Selected: ${manualSleepSliderValue.roundToInt()} hours",
                    style = MaterialTheme.typography.bodyLarge
                )
                if (showSlider) {
                    Slider(
                        value = manualSleepSliderValue,
                        onValueChange = { manualSleepSliderValue = it },
                        valueRange = 0f..24f,
                        steps = 23
                    )
                }
            }
        }

        Spacer(Modifier.height(9.dp))

        Text("Rate Sleep Quality", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(9.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            sleepQualityIcons.forEachIndexed { index, icon ->
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            if (sleepQuality == index) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            else Color.Transparent
                        )
                        .clickable { sleepQuality = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 24.sp)
                }
            }
        }

        Spacer(Modifier.height(27.dp))

        Button(
            onClick = {
                showDialog = true
            },
            enabled = !loggedToday,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 75.dp, vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (loggedToday) Color.Gray else MaterialTheme.colorScheme.primary
            )
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
    val targetSleep = 8f
    val progress = (sleepHours / targetSleep).coerceIn(0f, 1f)

    // Color logic
    val (progressColor, sleepFeedback) = when {
        sleepHours < 4 -> Pair(Color(0xFFFF6B6B), "Severely sleep-deprived")
        sleepHours < 6 -> Pair(Color(0xFFFFD166), "Mildly sleep-deprived")
        else -> Pair(Color(0xFF06D6A0), "Well-rested")
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with icon
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_nights_stay_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "SLEEP ANALYSIS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Main progress circle with detailed metrics
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = 1f,
                modifier = Modifier.size(150.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 8.dp
            )
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(150.dp),
                color = progressColor,
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.1f".format(sleepHours),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "hours",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = sleepFeedback.uppercase(),
                    color = progressColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sleep quality section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "QUALITY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp))

            // Star rating with emoji
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < sleepQuality) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (index < sleepQuality) Color(0xFFFFC107) else Color.LightGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when (sleepQuality) {
                        5 -> "😴 Deep sleep"
                        4 -> "😌 Restful"
                        3 -> "🫤 Light sleep"
                        2 -> "😣 Fragmented"
                        1 -> "😵 Tossing/turning"
                        else -> "No data"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Sleep recommendation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_lightbulb_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Recommendation",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            sleepHours < 6 -> "Aim for 7-9 hours. Try going to bed 30 mins earlier tonight."
                            else -> "Maintain your routine. Consistency improves sleep quality."
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
@Composable
fun JournalStreakProgress(
    entries: List<JournalEntry>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val last7Days = (0..6).map { today.minusDays(it.toLong()) }
    val weeklyEntries = entries.count { it.date in last7Days }
    val weeklyGoal = 7
    val currentStreak = calculateCurrentStreak(entries, today)
    val longestStreak = calculateLongestStreak(entries)
    val avgEntriesPerWeek = calculateWeeklyAverage(entries)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Header with dynamic title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_menu_book_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = when {
                    weeklyEntries == 0 -> "JUMPSTART YOUR WEEK"
                    weeklyEntries < 3 -> "BUILDING MOMENTUM"
                    weeklyEntries < 5 -> "GAINING TRACTION"
                    else -> "WEEKLY PROGRESS"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(9.dp))
        // Progress bars section
        Column(
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            // Current week completion
            ProgressStat(
                label = "This week",
                value = weeklyEntries,
                maxValue = weeklyGoal,
                icon = painterResource(id = R.drawable.baseline_calendar_month_24),
                color = MaterialTheme.colorScheme.primary
            )

            // Current streak
            ProgressStat(
                label = "Current streak",
                value = currentStreak,
                maxValue = longestStreak.coerceAtLeast(1),
                icon = painterResource(id = R.drawable.baseline_local_fire_department_24),
                color = if(currentStreak != 0) Color(0xFFFFA726) else MaterialTheme.colorScheme.primary
            )

            // Weekly average
            ProgressStat(
                label = "Weekly average",
                value = avgEntriesPerWeek.toInt(),
                maxValue = 7,
                icon = painterResource(id = R.drawable.baseline_analytics_24),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Motivational footer
        Text(
            text = when {
                weeklyEntries == weeklyGoal -> "You're crushing your journaling habit! Try adding morning pages tomorrow."
                weeklyEntries >= 5 -> "One more entry to match your average! What's on your mind today?"
                weeklyEntries >= 3 -> "Reflect on small wins - they add up to big progress."
                weeklyEntries >= 1 -> "Consistency beats intensity. Write a few lines every day."
                else -> "Start small: Write one sentence about today's highlight."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun ProgressStat(
    label: String,
    value: Int,
    maxValue: Int,
    icon: Painter,
    color: Color
) {
    val progress = value.toFloat() / maxValue.coerceAtLeast(1)

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "$value/${maxValue}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.12f)
        )
    }
}

// Helper functions
private fun calculateCurrentStreak(entries: List<JournalEntry>, today: LocalDate): Int {
    val loggedDates = entries.map { it.date }.toSet()
    var streak = 0
    var currentDate = today

    // Special case: if today is logged, start counting
    if (currentDate in loggedDates) {
        streak++
        currentDate = currentDate.minusDays(1)
    } else {
        // If today isn't logged, streak is 0 (even if yesterday was logged)
        return 0
    }

    // Count backwards until we find a missing day
    while (currentDate in loggedDates) {
        streak++
        currentDate = currentDate.minusDays(1)
    }

    return streak
}

private fun calculateLongestStreak(entries: List<JournalEntry>): Int {
    if (entries.isEmpty()) return 0

    val sortedDates = entries.map { it.date }.distinct().sorted()
    var longestStreak = 1
    var currentStreak = 1

    for (i in 1 until sortedDates.size) {
        if (sortedDates[i] == sortedDates[i-1].plusDays(1)) {
            currentStreak++
            longestStreak = maxOf(longestStreak, currentStreak)
        } else {
            currentStreak = 1 // Reset streak on gap
        }
    }

    return longestStreak
}
private fun calculateWeeklyAverage(entries: List<JournalEntry>): Float {
    if (entries.isEmpty()) return 0f
    val firstDate = entries.minOf { it.date }
    val weeks = ChronoUnit.WEEKS.between(firstDate, LocalDate.now()).coerceAtLeast(1)
    return entries.size.toFloat() / weeks
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun JournalingTracker(
    saveJournal: (JournalEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var journalEntry by remember { mutableStateOf("") }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var selectedJournalType by remember { mutableStateOf("scroll") }

    val journalTypes = listOf(
        "notebook" to Pair(painterResource(id = R.drawable.notebook_paper_ic), 300),
        "open_book" to Pair(painterResource(id = R.drawable.open_journal_book_1_ic_typ), 200),
        "sticky_note" to Pair(painterResource(id = R.drawable.sticky_note_ic), 100),
        "scroll" to Pair(painterResource(id = R.drawable.scroll_or_parchment_typ), 150)
    )

    val maxCharacters = journalTypes.first { it.first == selectedJournalType }.second
    val isOverLimit = journalEntry.length > maxCharacters.second
    val charCountColor = when {
        isOverLimit -> MaterialTheme.colorScheme.error
        journalEntry.length > maxCharacters.second * 0.9 -> Color(0xFFFFA726) // Orange
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ConfirmActionDialog(
            title = "Journalling",
            message = "Write your $selectedJournalType page? ",
            confirmButtonText = "Yes",
            onConfirm = {
                showLoadingDialog = true
                showDialog = false
            },
            onDismiss = { showDialog = false },
            icon = Icons.Default.CheckCircle)
    }

    if (showLoadingDialog) {
        JournalSaveDialog {
            saveJournal(
                JournalEntry(
                    date = LocalDate.now(),
                    entryText = journalEntry,
                    journalType = selectedJournalType
                )
            )
            journalEntry = ""
            showLoadingDialog = false
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        // Journal Type Selector
        Text(
            text = "Select Journal Style",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 7.dp),
            fontWeight = FontWeight.Bold
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(7.dp))
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .padding(5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2,
        ) {
            journalTypes.forEach { (type, pair) ->
                val (icon, _) = pair
                Box(modifier = Modifier.padding(4.dp)) {
                    FilterChip(
                        selected = selectedJournalType == type,
                        onClick = { selectedJournalType = type },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(100.dp)
                            ) {
                                Icon(
                                    icon,
                                    modifier = Modifier.size(25.dp),
                                    contentDescription = null,
                                    tint = Color.Unspecified
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    type.replace("_", " ").capitalize(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        modifier = Modifier.padding(bottom = 2.dp),
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedContainerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = FilterChipDefaults.elevatedFilterChipElevation(
                            pressedElevation = 2.dp
                        )
                    )
                }
            }
        }

        Text(
            text = "What are you grateful for today?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 3.dp),
            fontWeight = FontWeight.Bold
        )

        // Journal Input
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 1.dp,
                        shape = MaterialTheme.shapes.medium
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(270.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                        )
                        .padding(4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TextField(
                            value = journalEntry,
                            onValueChange = {
                                if (it.length <= maxCharacters.second * 1.1) journalEntry = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = {
                                Text(
                                    text = when (selectedJournalType) {
                                        "scroll" -> "Brief reflections..."
                                        "notebook" -> "Detailed thoughts..."
                                        else -> "Jot down your ideas..."
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            maxLines = when (selectedJournalType) {
                                "sticky_note" -> 3
                                else -> 6
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = "${maxCharacters.second - journalEntry.length}",
                            style = MaterialTheme.typography.labelSmall,
                            color = charCountColor,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Text(
                            text = "${journalEntry.length}/${maxCharacters.second}",
                            style = MaterialTheme.typography.labelSmall,
                            color = charCountColor
                        )
                    }
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                showDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 100.dp),
            enabled = journalEntry.isNotBlank() && !isOverLimit,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(painter = painterResource(id = R.drawable.baseline_draw_24), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Save Journal Entry")
        }
        Spacer(modifier = Modifier.height(275.dp))
    }
}

// Helper function for demo - replace with your actual affirmations
private fun getDailyAffirmation(): String {
    val affirmations = listOf(
        "I am capable of amazing things",
        "Today holds infinite possibilities",
        "I choose to focus on what matters most",
        "My challenges help me grow stronger"
    )
    return affirmations.random()
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
                val journalType = currentJournal.journalType.toJournalType()

                when (journalType) {
                    is ScrollBackground -> ScrollJournalView(
                        entryText = currentJournal.entryText,
                        date = currentJournal.date.toString(),
                        modifier = Modifier.fillMaxSize()
                    )

                    is NotebookBackground -> NotebookJournalView(
                        entryText = currentJournal.entryText,
                        date = currentJournal.date.toString(),
                        modifier = Modifier.fillMaxSize()
                    )

                    is StickyNoteBackground -> StickyNoteJournalView(
                        entryText = currentJournal.entryText,
                        date = currentJournal.date.toString(),
                        modifier = Modifier.fillMaxSize()
                    )

                    is OpenBookBackground -> OpenBookJournalView(
                        entryText = currentJournal.entryText,
                        date = currentJournal.date.toString(),
                        modifier = Modifier.fillMaxSize()
                    )
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
        // Main text positioned absolutely
        AnimatedText(
            text =  formatTextWithLineBreaks("$date\n$firstHalf",55),
            style = TextStyle(
                fontFamily = font,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Blue.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            delayPerChar = 50L, // Adjust speed here
            startDelay = 800L // Wait for background animation
        )
        AnimatedText(
            text =  formatTextWithLineBreaks(secondHalf,55),
            style = TextStyle(
                fontFamily = font,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Blue.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = 1.dp),
            delayPerChar = 50L, // Adjust speed here
            startDelay = 2000L // Wait for background animation
        )

    }
}

@Composable
fun JournalSaveDialog(
    onFinished: () -> Unit
) {
    var isSaving by remember { mutableStateOf(true) }
    val animatedEllipsis by animateEllipsis()

    // Automatically transition after 2 seconds
    LaunchedEffect(Unit) {
        delay(2000)
        isSaving = false
        delay(1000)
        onFinished()
    }

    Dialog(onDismissRequest = { /* Block manual dismiss */ }) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(200.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Writing",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = animatedEllipsis,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
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

@Composable
fun animateEllipsis(): State<String> {
    val ellipsisStates = listOf("", ".", "..", "...")
    var index by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(300)
            index = (index + 1) % ellipsisStates.size
        }
    }

    return remember { derivedStateOf { ellipsisStates[index] } }
}




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
    var showSavingDialog by remember{ mutableStateOf(false)}
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ConfirmActionDialog(
            title = "Exercise Tracker",
            message = "Save $exerciseName for $time?",
            confirmButtonText = "Log",
            onConfirm = {
                showSavingDialog = true
                showDialog = false
            },
            onDismiss = { showDialog = false },
            icon = Icons.Default.CheckCircle)
    }



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


    Column {
        Box(modifier = Modifier.fillMaxSize()) {
            if (showSavingDialog){
                AnimatedSaveDialog(
                    loadingText = "Logging exercise time",
                    successText = "Saved"
                ) {
                    val loggedExercise = ExerciseData(
                        exerciseName = exerciseName,
                        timeElapsed = time.toFloat(),
                        exerciseDate = LocalDate.now().toString()
                    )
                    onTimerStopped(loggedExercise)
                    clearTimerState()
                    showSavingDialog = false
                }
            }
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(
                            topStart = 5.dp, topEnd = 5.dp,
                            bottomStart = 5.dp, bottomEnd = 5.dp
                        )
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 5.dp, topEnd = 5.dp,
                            bottomStart = 5.dp, bottomEnd = 5.dp
                        )
                    ),
                painter = painterResource(id = R.drawable.background_exercise_timer),
                contentDescription = null
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .shadow(
                            elevation = 2.dp, shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(13.dp)
                        )
                        .padding(13.dp)
                ) {
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { updateExerciseName(it) },
                        label = { Text("Enter Exercise Name") },
                        singleLine = true,
                        readOnly = isRunning,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(27.dp))
                }
                Spacer(modifier = Modifier.height(46.dp))
                val displayTime = if (time < 60) "$time s" else "${time / 60}m ${time % 60}s"

                TimerContainer(
                    time = displayTime,
                    glowWhenRunning = isTimerRunning
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp)
        ) {
            Button(
                modifier = Modifier.align(Alignment.Center),
                shape = RoundedCornerShape(5.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(5.dp),
                enabled = exerciseName.isNotBlank(),
                onClick = {
                    if (isRunning) {
                        stopTimerService(context)
                        isRunning = false
                    } else {

                        // Check for Notification Permission on Android 13+
                        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
            if (!isRunning && time != 0) {
                Button(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    shape = RoundedCornerShape(5.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(1.dp),
                    onClick = {
                        showDialog = true
                    }
                ) {
                    Text(text = "Log Time")
                }
                Button(
                    modifier = Modifier.align(Alignment.CenterStart),
                    shape = RoundedCornerShape(5.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(1.dp),
                    onClick = {
                        clearTimerState()
                    }
                ) {
                    Text(text = "Reset")
                }

            }
        }
    }
}



private fun startTimerService(context: Context) {
    Log.d("ExerciseTimer", "Requesting Service Start")
    val serviceIntent = Intent(context, ExerciseTimerService::class.java).apply {
        action = ExerciseTimerService.ACTION_START_SERVICE
    }
    ContextCompat.startForegroundService(context, serviceIntent)
}

private fun stopTimerService(context: Context) {
    val serviceIntent = Intent(context, ExerciseTimerService::class.java).apply {
        action = ExerciseTimerService.ACTION_STOP_SERVICE
    }
    context.startService(serviceIntent)
}

@Composable
fun TimerContainer(
    time: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = Color(0xFF4285F4),
    glowWhenRunning: Boolean = false,
    content: @Composable () -> Unit = {
        Text(
            text = time,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A),
            style = MaterialTheme.typography.bodyLarge
        )
    }
) {
    // Animate rotation for inner snake glow
    val infiniteTransition = rememberInfiniteTransition(label = "Snake Inner Rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Inner Arc Rotation"
    )

    Surface(
        modifier = modifier
            .size(160.dp),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Inner rotating arc
            if (glowWhenRunning) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 6.dp.toPx()
                    val sweepAngle = 90f // Snake segment length
                    val inset = strokeWidth // keeps it inside

                    rotate(rotationAngle, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    highlightColor.copy(alpha = 0.25f),
                                    highlightColor,
                                    highlightColor.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - 2 * inset, size.height - 2 * inset),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // Timer content + icon
            Box(
                modifier = Modifier
                    .size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
                val context = LocalContext.current
                val imageLoader = ImageLoader.Builder(context)
                    .components(fun ComponentRegistry.Builder.() {
                        add(ImageDecoderDecoder.Factory())
                    })
                    .build()
                if (glowWhenRunning){
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(R.drawable.runnin_man_gif)
                            .build(),
                        contentDescription = null,
                        imageLoader = imageLoader,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = (-10).dp)
                    )
                }else{
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_directions_run_24),
                        contentDescription = "Running Icon",
                        tint = Color.DarkGray.copy(alpha = 0.75f),
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = (-10).dp)
                    )
                }
            }
        }
    }
}
@Composable
fun AnimatedSaveDialog(
    loadingText: String = "Saving",
    successText: String = "Saved!",
    successIcon: ImageVector = Icons.Default.CheckCircle,
    durationMillis: Int = 2000,
    successDurationMillis: Int = 1000,
    onFinished: () -> Unit
) {
    var isSaving by remember { mutableStateOf(true) }
    val animatedEllipsis by animateEllipsis()

    // Automatically transition to success, then finish
    LaunchedEffect(Unit) {
        delay(durationMillis.toLong())
        isSaving = false
        delay(successDurationMillis.toLong())
        onFinished()
    }

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(200.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = loadingText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = animatedEllipsis,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    Icon(
                        imageVector = successIcon,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = successText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun DailyHabitsTopBar(
    onBackIconClick: () -> Unit,
    modifier : Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 27.dp,
            )
            .background(MaterialTheme.colorScheme.inversePrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        onClick = { onBackIconClick() },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Back",
                    modifier = Modifier
                        .rotate(180f)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.surfaceTint
                )
            }

            // Title
            Text(
                text = "Daily Habits",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            // Right-side Icon
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                shadowElevation = 2.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.daily_habits),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewDailyHabitsScreen() {
 //   DailyHabitsScreen()
}

