package com.example.halocare.ui.presentation.charts

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halocare.BuildConfig
import com.example.halocare.ui.models.HaloMoodEntry
import com.example.halocare.ui.presentation.responsiveHeight
import com.example.halocare.ui.presentation.responsiveWidth
import kotlinx.coroutines.delay
import responsiveSp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * MoodChartBackground composable handles the sky gradient, landscape, and hour markings
 */
// ty claude, ty gemini
@Composable
fun MoodChartBackground(
    hour: Int,
    hourWidthPx: Dp,
    skyColors: List<Pair<Int, Color>>,
    vegetationBaseColors: List<Pair<Int, Color>>,
    vegetationHighlightColors: List<Pair<Int, Color>>,
    textMeasurer: TextMeasurer,
    content: @Composable BoxScope.() -> Unit
) {
    // Helper function to interpolate colors
    fun getInterpolatedColor(colorStops: List<Pair<Int, Color>>, hour: Int): Color {
        val beforeStop = colorStops.lastOrNull { it.first <= hour } ?: colorStops.first()
        val afterStop = colorStops.firstOrNull { it.first > hour } ?: colorStops.last()

        if (beforeStop.first == hour) return beforeStop.second
        if (afterStop.first == hour) return afterStop.second

        val range = afterStop.first - beforeStop.first
        val position = (hour - beforeStop.first).toFloat() / range

        return lerp(beforeStop.second, afterStop.second, position)
    }

    val skyColor = getInterpolatedColor(skyColors, hour)
    val nextSkyColor = getInterpolatedColor(skyColors, (hour + 1) % 24)
    val vegetationBase = getInterpolatedColor(vegetationBaseColors, hour)
    val vegetationHighlight = getInterpolatedColor(vegetationHighlightColors, hour)

    val skyGradient = Brush.horizontalGradient(listOf(skyColor, nextSkyColor))

    Box(
        modifier = Modifier
            .width(hourWidthPx)
            .height(400.dp.responsiveHeight())
            .background(skyGradient)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val vegetationHeight = height * 0.3f

            // Draw vegetation base
            val vegetationPath = Path().apply {
                moveTo(0f, height - vegetationHeight)
                lineTo(width, height - vegetationHeight)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            val vegetationGradient = Brush.verticalGradient(
                colors = listOf(vegetationHighlight, vegetationBase),
                startY = height - vegetationHeight,
                endY = height
            )

            drawPath(
                path = vegetationPath,
                brush = vegetationGradient
            )

            // Draw subtle vegetation silhouette
            val silhouettePath = Path().apply {
                val baseY = height - vegetationHeight
                moveTo(0f, baseY)

                // Create a subtle undulating landscape
                for (i in 0..10) {
                    val x = i * (width / 10)
                    val yOffset = (10 * Math.sin(i * 0.8)).toFloat() // Subtle hills
                    cubicTo(
                        x - 20f, baseY + yOffset - 10f,
                        x - 10f, baseY + yOffset - 20f,
                        x, baseY + yOffset
                    )
                }

                lineTo(width, baseY)
                close()
            }

            // Draw the silhouette in a slightly darker color
            drawPath(
                path = silhouettePath,
                color = vegetationBase.copy(alpha = 0.7f)
            )

            // Draw hour divider line
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(0f, 0f),
                end = Offset(0f, height - vegetationHeight),
                strokeWidth = 2f
            )

            // Draw hour text
            drawText(
                textMeasurer = textMeasurer,
                text = "${hour}:00",
                topLeft = Offset(width / 2 - 15, 20f),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp,
                    shadow = Shadow(
                        color = Color.Black,
                        blurRadius = 3f
                    )
                )
            )
        }

        // Content slot for mood entries
        content()
    }
}

/**
 * MoodTimeline composable handles the mood entries and interactions for a specific hour
 */
@Composable
fun MoodTimeline(
    hour: Int,
    hourWidth: Dp,
    skyColors: List<Pair<Int, Color>>,
    vegetationBaseColors: List<Pair<Int, Color>>,
    vegetationHighlightColors: List<Pair<Int, Color>>,
    landscapePoints: List<Float>,
    hourEntries: List<HaloMoodEntry>,
    allMoodPositions: List<Pair<Float, Float>>,
    textMeasurer: TextMeasurer,
    entryContent: @Composable (List<HaloMoodEntry>, Offset, HaloMoodEntry?, (HaloMoodEntry) -> Unit) -> Unit,
    markerContent: @Composable (HaloMoodEntry, Offset) -> Unit,
    fixedEntryYPositionPx: Float,
) {
    var selectedMoodEntry by remember { mutableStateOf<HaloMoodEntry?>(null) }
    val density = LocalDensity.current
    val hourWidthPx = remember(hourWidth, density) {
        with(density) { hourWidth.toPx() }
    }
    // --- Add Internal Padding ---
    val horizontalPaddingDp = 4.dp
    val horizontalPaddingPx = remember(horizontalPaddingDp, density) {
        with(density) { horizontalPaddingDp.toPx() }
    }
    val effectiveWidthPx = (hourWidthPx - 2 * horizontalPaddingPx).coerceAtLeast(0f)
    val bucketWidthPx = effectiveWidthPx / 6f
    val bucketData = remember(hourEntries) { mutableMapOf<Int, Pair<Offset, List<HaloMoodEntry>>>() }

    val entriesByBucket = remember(hourEntries) {
        groupEntriesByMinuteBucket(hourEntries)
            .also { Log.d("MoodBucket", "Hour $hour buckets: ${it.mapValues { entry -> entry.value.size }}") }
    }
    val iconSizeDp = 30.dp
    val iconSpacingDp = 4.dp
    val iconHeightPx = remember(iconSizeDp, density) { with(density) { iconSizeDp.toPx() } }
    val iconSpacingPx = remember(iconSpacingDp, density) { with(density) { iconSpacingDp.toPx() } }


    val onEntryClickHandler: (HaloMoodEntry) -> Unit = { clickedEntry ->
        selectedMoodEntry = if (selectedMoodEntry == clickedEntry) null else clickedEntry }

    MoodChartBackground(
        hour = hour,
        hourWidthPx = hourWidth,
        skyColors = skyColors,
        vegetationBaseColors = vegetationBaseColors,
        vegetationHighlightColors = vegetationHighlightColors,
        textMeasurer = textMeasurer
    ) {
        // Draw connecting lines between entries (uses the fixed Y position)
        Canvas(modifier = Modifier.matchParentSize()) {
            val globalStartX = hour * hourWidthPx
            val globalEndX = globalStartX + size.width

            if (allMoodPositions.size > 1) {
                val path = Path()
                var pathStarted = false

                for (i in 0 until allMoodPositions.size - 1) {
                    val start = allMoodPositions[i]
                    val end = allMoodPositions[i + 1]

                    val startXGlobal = start.first
                    val endXGlobal = end.first
                    val yPos = start.second
                    if ((startXGlobal <= globalEndX && endXGlobal >= globalStartX)) {
                        val visibleStartX = (startXGlobal - globalStartX).coerceIn(0f, size.width)
                         val visibleEndX = (endXGlobal - globalStartX).coerceIn(0f, size.width)

                          if (visibleEndX > visibleStartX) {
                            if (!pathStarted) {
                                path.moveTo(visibleStartX, yPos)
                                pathStarted = true
                            }
                            path.lineTo(visibleEndX, yPos)
                        }
                    }
                }

                // Draw the path if it contains segments
                if (pathStarted) {
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.7f),
                        style = Stroke(
                            width = 4f, // Thinner line?
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                        )
                    )
                }
            }
        }

        // Draw entries and handle clicks
        Box(modifier = Modifier
            .fillMaxSize()
        ) {
            for ((bucketIndex, entriesInBucket) in entriesByBucket) {
                val bucketCenterXPx = horizontalPaddingPx + (bucketIndex * bucketWidthPx) + (bucketWidthPx / 2f)
                val bucketPosition = Offset(bucketCenterXPx, fixedEntryYPositionPx)

                bucketData[bucketIndex] = Pair(bucketPosition, entriesInBucket)

                entryContent(
                    entriesInBucket,
                    bucketPosition,
                    selectedMoodEntry,
                    onEntryClickHandler
                )
            }

            // --- Draw Marker ---
            selectedMoodEntry?.let { selectedEntry ->
                // Find the bucket this selected entry belongs to
                var bucketPosition: Offset? = null
                var entriesInBucket: List<HaloMoodEntry>? = null
                var foundBucketIndex = -1

                for ((index, data) in bucketData) {
                    if (data.second.contains(selectedEntry)) {
                        bucketPosition = data.first
                        entriesInBucket = data.second
                        foundBucketIndex = index
                        break
                    }
                }

                // If found, calculate the specific Y position of the selected icon within its column
                if (bucketPosition != null && entriesInBucket != null) {
                    val entryIndex = entriesInBucket.indexOf(selectedEntry)

                    if (entryIndex != -1) {
                        val columnTopY = bucketPosition.y - (iconHeightPx / 2f)
                        val verticalOffsetToIconTop = entryIndex * (iconHeightPx + iconSpacingPx)
                        val iconCenterY = columnTopY + verticalOffsetToIconTop + (iconHeightPx / 2f)

                        val markerTargetPosition = Offset(bucketPosition.x, iconCenterY)

                        markerContent(selectedEntry, markerTargetPosition)
                    } else {
                        Log.e("MarkerPos", "Selected entry not found in its supposed bucket list!")
                    }
                }
            }
        }
    }
}

/**
 * Groups entries into 10-minute buckets for a given hour.
 * Returns a Map where the key is the bucket index (0-5) and the value is the list of entries in that bucket.
 */
private fun groupEntriesByMinuteBucket(
    hourEntries: List<HaloMoodEntry>
): Map<Int, List<HaloMoodEntry>> {
    val buckets = List(6) { mutableListOf<HaloMoodEntry>() }

    val calendar = Calendar.getInstance()

    for (entry in hourEntries) {
        calendar.timeInMillis = entry.timeLogged
        val minute = calendar.get(Calendar.MINUTE)
        val bucketIndex = (minute / 10).coerceIn(0, 5)
        buckets[bucketIndex].add(entry)
    }

    buckets.forEach { it.sortBy { entry -> entry.timeLogged } }

    // Convert to Map, removing empty buckets
    return buckets.withIndex()
        .filter { it.value.isNotEmpty() }
        .associate { it.index to it.value.toList() }
}

/**
 * Displays a group of mood entries, typically as a row of icons.
 */
@Composable
fun GroupedMoodEntry(
    entriesInGroup: List<HaloMoodEntry>,
    key : Any?,
    position: Offset,
    isSelected: Boolean,
    iconSize : Dp = 30.dp,
    iconSpacing : Dp = 4.dp,
    selectedEntry: HaloMoodEntry?,
    onEntryClick: (HaloMoodEntry) -> Unit
) {

    val density = LocalDensity.current

    val iconWidthPx = with(density) { iconSize.toPx() }

    val startOffsetXDp = with(density) { (position.x - iconWidthPx / 2).toDp() }
    val startOffsetYDp = with(density) { (position.y - iconWidthPx / 2f).toDp() }


    val selectionShape = RoundedCornerShape(8.dp)
    val selectionColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)


    var hasAppeared by remember(key) { mutableStateOf(false) }
    var iconsAppeared by remember(key) { mutableStateOf(false) }

    LaunchedEffect(key) {
        hasAppeared = true
        delay(100) // Small delay before starting icon animations
        iconsAppeared = true
    }

    AnimatedVisibility(
        visible = hasAppeared,
        enter = scaleIn(initialScale = 0.1f) + fadeIn()
    ) {
        Box(
            contentAlignment = Alignment.TopStart,
            modifier = Modifier
                .offset(x = startOffsetXDp, y = startOffsetYDp)
                .then(
                    if (isSelected) Modifier.background(
                        selectionColor,
                        selectionShape
                    ) else Modifier
                )
                .padding(horizontal = 4.dp, vertical = 8.dp)

        ) {
            Column(
                modifier = Modifier
                    .width(iconSize)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(iconSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                val iconScale by animateFloatAsState(
                    targetValue = if (iconsAppeared) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                )

                entriesInGroup.forEach { entry ->
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .scale(iconScale)
                            .clickable(
                                onClick = { onEntryClick(entry) },
                                indication = rememberRipple(
                                    bounded = false,
                                    radius = iconSize / 2 + 4.dp
                                ),
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center

                    ) {
                        Icon(
                            painter = painterResource(id = entry.iconRes),
                            contentDescription = entry.gist,
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}

/**
 * DefaultMoodMarker composable provides the default text bubble for HaloMoodEntry
 */
@Composable
fun DefaultMoodMarker(
    entry: HaloMoodEntry,
    position: Offset,
) {
    val timeString = formatTimestampToHHmm(entry.timeLogged)

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val bubbleWidth = 120.dp.responsiveWidth()
        val bubbleWidthPx = with(LocalDensity.current) { bubbleWidth.toPx() }
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }

        val desiredX = position.x - bubbleWidthPx / 2
        val adjustedX = desiredX.coerceIn(0f, maxWidthPx - bubbleWidthPx)

        val markerOffsetY = 30.dp
        val density = LocalDensity.current

        val markerYPx = position.y + with(density) { markerOffsetY.toPx() }
        val markerYDp = with(density) { markerYPx.toDp() }
        val triangleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)

        Box(
            modifier = Modifier
                .width(bubbleWidth)
                .wrapContentHeight()
                .absoluteOffset(
                    x = with(density) { adjustedX.toDp() },
                    y = markerYDp
                )
        ) {
            val pointerSize = 12.dp
            val pointerSizePx = with(density) { pointerSize.toPx() }

            val pointerShiftPx = position.x - (adjustedX + bubbleWidthPx / 2)

            val pointerOffsetX = with(density) { pointerShiftPx.toDp() }

            // Triangle pointer Canvas (pointing upwards)
            Canvas(
                modifier = Modifier
                    .size(pointerSize)
                    .align(Alignment.TopCenter)
                    .offset(x = pointerOffsetX, y = -(pointerSize / 2))
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2, 0f)
                    lineTo(0f, size.height)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(
                    path = path,
                    color = triangleColor
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(8.dp),
                        clip = false
                    )
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = entry.gist,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp.responsiveSp(),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp.responsiveSp()
                )
                Spacer(modifier = Modifier.height(4.dp.responsiveHeight()))
                Text(
                    text = timeString,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Complete MoodChart composable that puts everything together using HaloMoodEntry
 */
@Composable
fun MoodChart(
    moodEntries: List<HaloMoodEntry>, // Changed data type
    hourRange: IntRange = 0..23,
    hourWidth: Dp = 150.dp.responsiveWidth(),
    iconSize: Dp = 30.dp.responsiveHeight(),
    iconSpacing: Dp = 4.dp,
    skyColors: List<Pair<Int, Color>> = defaultSkyColors,
    vegetationBaseColors: List<Pair<Int, Color>> = defaultVegetationBaseColors,
    vegetationHighlightColors: List<Pair<Int, Color>> = defaultVegetationHighlightColors,
    landscapePoints: List<Float> = defaultLandscapePoints,
    fixedEntryYPositionPx: Float = 250f.responsiveHeight(),
    entryContent: @Composable (List<HaloMoodEntry>, Offset, HaloMoodEntry?, (HaloMoodEntry) -> Unit) -> Unit = { entries, position, currentSelectedEntry, clickHandler ->
        GroupedMoodEntry(
            entriesInGroup = entries,
            position = position,
            iconSize = iconSize,
            iconSpacing = iconSpacing,
            isSelected = false,
            selectedEntry = currentSelectedEntry,
            onEntryClick = clickHandler,
            key = entries.firstOrNull()?.id ?: entries.hashCode()
        )
    },
    markerContent: @Composable (HaloMoodEntry, Offset) -> Unit = { entry, position ->
        DefaultMoodMarker(entry, position)
    }
) {
    val textMeasurer = rememberTextMeasurer()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val hourWidthPx = remember(hourWidth, density) {
        with(density){hourWidth.toPx()}
    }
    // Calculate all mood positions for the connecting line (sorted by time, fixed Y)
    val allMoodPositions = remember(moodEntries, hourWidthPx, fixedEntryYPositionPx) {
        moodEntries
            .sortedBy { it.timeLogged }
            .map { entry ->
                val floatHour = convertTimestampToFloatHour(entry.timeLogged)
                Pair(floatHour * hourWidthPx, fixedEntryYPositionPx)
            }
    }


    // Draw timeline using horizontal scroll
    Row(Modifier.horizontalScroll(scrollState)) {
        for (hour in hourRange) {
            val hourEntries = remember(moodEntries, hour) {
                moodEntries.filter { entry ->
                    val floatHour = convertTimestampToFloatHour(entry.timeLogged)
                    floatHour >= hour && floatHour < hour + 1
                }
            }

            MoodTimeline(
                hour = hour,
                hourWidth = hourWidth,
                skyColors = skyColors,
                vegetationBaseColors = vegetationBaseColors,
                vegetationHighlightColors = vegetationHighlightColors,
                landscapePoints = landscapePoints,
                hourEntries = hourEntries,
                allMoodPositions = allMoodPositions,
                textMeasurer = textMeasurer,
                entryContent = entryContent,
                markerContent = markerContent,
                fixedEntryYPositionPx = fixedEntryYPositionPx
            )
        }
    }

    val firstEntryTimestamp = moodEntries.minByOrNull { it.timeLogged }?.timeLogged

    LaunchedEffect(firstEntryTimestamp) {
        val now = Calendar.getInstance()
        val isTodayList = moodEntries.isNotEmpty() && moodEntries.all { entry ->
            val entryCal = Calendar.getInstance().apply { timeInMillis = entry.timeLogged }
            entryCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    entryCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
        }

        val targetHour = if (isTodayList) {
            (now.get(Calendar.HOUR_OF_DAY) - 1).coerceIn(hourRange.first, hourRange.last)
        } else {
            firstEntryTimestamp?.let {
                convertTimestampToFloatHour(it).toInt().coerceIn(hourRange.first, hourRange.last)
            } ?: (now.get(Calendar.HOUR_OF_DAY) - 1).coerceIn(hourRange.first, hourRange.last)
        }
        val targetScrollOffsetPx = (targetHour * hourWidthPx).roundToInt()

        delay(150)

        val maxScroll = scrollState.maxValue

        if (targetScrollOffsetPx >= 0) {
            if (maxScroll > 0) {
                val coercedTarget = targetScrollOffsetPx.coerceAtMost(maxScroll)
                scrollState.animateScrollTo(
                    value = coercedTarget,
                    animationSpec = tween(
                        durationMillis = 2000,
                        easing = EaseInOut
                    )
                )
            } else if (targetScrollOffsetPx == 0) {
                scrollState.animateScrollTo(0)
            } else {
                Log.w("InitialScroll", "Max scroll value is still 0. Cannot scroll to $targetScrollOffsetPx.")
            }
        } else {
            Log.w("InitialScroll", "Target scroll offset negative ($targetScrollOffsetPx), not scrolling.")
        }
    }
}

fun convertTimestampToFloatHour(timestamp: Long): Float {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp

        // timeZone = TimeZone.getDefault()
    }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return hour + (minute / 60.0f)
}

fun formatTimestampToHHmm(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())

    // format.timeZone = TimeZone.getDefault()
    return format.format(date)
}

// Default values for colors and landscape
val defaultSkyColors = listOf(
    Pair(0, Color(0xFF0C1445)),  // Midnight
    Pair(5, Color(0xFF1E3D59)),  // Dawn
    Pair(6, Color(0xFF7FB2F0)),  // Sunrise
    Pair(12, Color(0xFF87CEEB)), // Noon
    Pair(18, Color(0xFFFF9E7B)), // Sunset
    Pair(20, Color(0xFF1E3D59)), // Dusk
    Pair(22, Color(0xFF0C1445))  // Night
)

val defaultVegetationBaseColors = listOf(
    Pair(0, Color(0xFF0C2818)),  // Night
    Pair(6, Color(0xFF0F3823)),  // Dawn
    Pair(8, Color(0xFF175E3A)),  // Morning
    Pair(12, Color(0xFF1A6B42)), // Noon
    Pair(17, Color(0xFF175E3A)), // Afternoon
    Pair(20, Color(0xFF0F3823)), // Evening
    Pair(22, Color(0xFF0C2818))  // Night
)

val defaultVegetationHighlightColors = listOf(
    Pair(0, Color(0xFF175E3A)),   // Night
    Pair(6, Color(0xFF1A6B42)),   // Dawn
    Pair(8, Color(0xFF1F7F4E)),   // Morning
    Pair(12, Color(0xFF228B57)),  // Noon
    Pair(17, Color(0xFF1F7F4E)),  // Afternoon
    Pair(20, Color(0xFF1A6B42)),  // Evening
    Pair(22, Color(0xFF175E3A))   // Night
)

val defaultLandscapePoints = listOf(0f, 5f, 10f, 7f, 15f, 10f, 5f, 0f)

@Composable
fun Float.responsiveWidth(): Float {
    if (!BuildConfig.IS_USER_BUILD) return this
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val scaleFactor = (screenWidth.toFloat() / 411f).coerceIn(0.8f, 1.2f)
    return this * scaleFactor
}

@Composable
fun Float.responsiveHeight(): Float {
    if (!BuildConfig.IS_USER_BUILD) return this
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val scaleFactor = (screenHeight.toFloat() / 891f).coerceIn(0.8f, 1.2f)
    return this * scaleFactor
}