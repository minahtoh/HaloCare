package com.example.halocare.ui.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halocare.R

/**
 * MoodChartBackground composable handles the sky gradient, landscape, and hour markings
 */

// ty claude
@Composable
fun MoodChartBackground(
    hour: Int,
    hourWidthPx: Float,
    skyColors: List<Pair<Int, Color>>,
    vegetationBaseColors: List<Pair<Int, Color>>,
    vegetationHighlightColors: List<Pair<Int, Color>>,
    landscapePoints: List<Float>,
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

    // Get colors for this hour
    val skyColor = getInterpolatedColor(skyColors, hour)
    val nextSkyColor = getInterpolatedColor(skyColors, (hour + 1) % 24)
    val vegetationBase = getInterpolatedColor(vegetationBaseColors, hour)
    val vegetationHighlight = getInterpolatedColor(vegetationHighlightColors, hour)

    // Create gradient for this hour
    val skyGradient = Brush.horizontalGradient(listOf(skyColor, nextSkyColor))

    Box(
        modifier = Modifier
            .width(hourWidthPx.dp)
            .height(400.dp)
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

            // Draw landscape silhouette
            val silhouettePath = Path().apply {
                val baseY = height - vegetationHeight
                moveTo(0f, baseY + landscapePoints.first())

                landscapePoints.forEachIndexed { index, yOffset ->
                    if (index > 0) {
                        val x = (index - 1) * (width / (landscapePoints.size - 1))
                        lineTo(x, baseY + yOffset)
                    }
                }

                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

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
 * MoodTimeline composable handles the mood entries and interactions
 */
@Composable
fun MoodTimeline(
    hour: Int,
    hourWidthPx: Float,
    skyColors: List<Pair<Int, Color>>,
    vegetationBaseColors: List<Pair<Int, Color>>,
    vegetationHighlightColors: List<Pair<Int, Color>>,
    landscapePoints: List<Float>,
    moodEntries: List<MoodEntry>,
    allMoodPositions: List<Pair<Float, Float>>,
    textMeasurer: TextMeasurer,
    entryContent: @Composable (MoodEntry, Offset, Boolean) -> Unit,
    markerContent: @Composable (MoodEntry, Offset) -> Unit
) {
    // State to track selected emoji
    var selectedMoodEntry by remember { mutableStateOf<MoodEntry?>(null) }

    // Create a map of entry positions for click detection
    val entryPositions = remember(moodEntries) { mutableMapOf<MoodEntry, Offset>() }

    // Fixed Y position for all entries
    val entryYPosition = 300 * 0.5f

    val entryYPositionDp = with(LocalDensity.current) { entryYPosition.toDp() }

    MoodChartBackground(
        hour = hour,
        hourWidthPx = hourWidthPx,
        skyColors = skyColors,
        vegetationBaseColors = vegetationBaseColors,
        vegetationHighlightColors = vegetationHighlightColors,
        landscapePoints = landscapePoints,
        textMeasurer = textMeasurer
    ) {
        // Draw connecting lines between entries
        Canvas(modifier = Modifier.matchParentSize()) {
            // Calculate global positions for drawing the mood connection line
            val globalStartX = hour * hourWidthPx
            val globalEndX = globalStartX + size.width

            // Draw the dashed line
            if (allMoodPositions.size > 1) {
                val path = Path()
                var pathStarted = false

                // Find all line segments that pass through this hour segment
                for (i in 0 until allMoodPositions.size - 1) {
                    val start = allMoodPositions[i]
                    val end = allMoodPositions[i + 1]

                    val startX = start.first
                    val endX = end.first

                    // Check if this line segment intersects with our hour segment
                    if ((startX <= globalEndX && endX >= globalStartX)) {
                        // Calculate visible portion within this hour segment
                        val visibleStartX = (startX - globalStartX).coerceIn(0f, size.width)
                        val visibleEndX = (endX - globalStartX).coerceIn(0f, size.width)

                        if (!pathStarted) {
                            path.moveTo(visibleStartX, entryYPosition)
                            pathStarted = true
                        }

                        path.lineTo(visibleEndX, entryYPosition)
                    }
                }

                // Draw the path with dash effect
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                    )
                )
            }
        }

        // Draw entries and handle clicks
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Find the closest entry to the tap position
                    var closestEntry: MoodEntry? = null
                    var minDistance = Float.MAX_VALUE

                    entryPositions.forEach { (entry, position) ->
                        val distance = (offset - position).getDistance()
                        if (distance < minDistance && distance < 30f) { // 30f is tap radius
                            minDistance = distance
                            closestEntry = entry
                        }
                    }

                    // Toggle selection
                    selectedMoodEntry =
                        if (selectedMoodEntry == closestEntry) null else closestEntry
                }
            }
        ) {
            // Draw each mood entry
            // For each mood entry, wrap it in its own clickable Box
            for (entry in moodEntries) {
                val minutePosition = (entry.time - hour.toFloat()) * hourWidthPx
                val entryPosition = Offset(minutePosition, entryYPosition)

                // Store entry position for click detection
                entryPositions[entry] = entryPosition

                // Draw the entry content with its own clickable area
                Box(
                    modifier = Modifier
                        .offset(x = minutePosition.dp, y = entryYPositionDp)
                        .clickable {
                            // Direct toggle on click
                            selectedMoodEntry = if (selectedMoodEntry == entry) null else entry
                        }
                ) {
                    entryContent(entry, entryPosition, entry == selectedMoodEntry)
                }
            }

            // Draw marker for selected entry
            selectedMoodEntry?.let { entry ->
                val position = entryPositions[entry] ?: return@let

                // Call the custom marker content composable
                markerContent(entry, position)
            }
        }
    }
}


/**
 * DefaultMoodEntry composable provides the default emoji representation
 */
@Composable
fun DefaultMoodEntry(
    entry: MoodEntry,
    position: Offset ,
    isSelected: Boolean,
    textMeasurer: TextMeasurer
) {
    Canvas(modifier = Modifier
        .size(36.dp)) {
        // Draw emoji
        drawText(
            textMeasurer = textMeasurer,
            text = entry.emoji,
            topLeft = Offset(-12f, -12f),
            style = TextStyle(fontSize = 24.sp)
        )

        // Draw circle around the emoji
        drawCircle(
            color = if (isSelected) Color.Yellow else Color.White,
            radius = 18f,
            center = Offset(0f, 0f),
            style = if (isSelected)
                Stroke(width = 3f)
            else
                Stroke(width = 2f)
        )
    }
}

/**
 * CustomMoodEntry composable provides a version that uses an icon/image instead of text emoji
 */
@Composable
fun CustomMoodEntry(
    position: Offset,
    isSelected: Boolean,
    iconContent: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        // Place the icon content in the center
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            iconContent()
        }
    }
}

/**
 * DefaultMoodMarker composable provides the default text bubble
 */
@Composable
fun DefaultMoodMarker(
    entry: MoodEntry,
    position: Offset,
    hourWidthPx: Float
) {
    val hour = (position.x / hourWidthPx).toInt()
    val minutes = ((entry.time - hour) * 60).toInt()
    val timeString = "${hour}:${minutes.toString().padStart(2, '0')}"

    // Use BoxWithConstraints to get available screen width
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val bubbleWidth = 100.dp
        val bubbleWidthPx = with(LocalDensity.current) { bubbleWidth.toPx() }
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }

        // Calculate adjusted X position to keep marker entirely visible
        val adjustedX = position.x.coerceIn(
            bubbleWidthPx / 2,
            maxWidthPx - bubbleWidthPx / 2
        )

        // Position the bubble
        Box(
            modifier = Modifier
                .width(bubbleWidth)
                .wrapContentHeight()
                .absoluteOffset(
                    x = with(LocalDensity.current) { (adjustedX - bubbleWidthPx / 2).toDp() },
                    y = with(LocalDensity.current) { (position.y + 100).toDp() }
                )
                .align(Alignment.Center)
        ) {
            // Triangle pointer
            val emojiPosition = position.x
            val triangleColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)

            Canvas(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
            ) {
                val path = Path().apply {
                    moveTo(emojiPosition / 2, 0f)
                    lineTo(0f, size.height)
                    lineTo(emojiPosition, size.height)
                    close()
                }
                drawPath(
                    path = path,
                    color = triangleColor
                )
            }

            // Content bubble
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 7.dp,
                        shape = RoundedCornerShape(8.dp),
                        clip = false
                    )
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                // Draw text content bubble
                Text(
                    text = entry.text,
                    color = Color.Black,
                    fontSize = 11.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Draw timestamp
                Text(
                    text = timeString,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
/**
 * Complete MoodChart composable that puts everything together
 */
@Composable
fun MoodChart(
    moodEntries: List<MoodEntry>,
    hourRange: IntRange = 0..23,
    hourWidthPx: Float = 50f,
    skyColors: List<Pair<Int, Color>> = defaultSkyColors,
    vegetationBaseColors: List<Pair<Int, Color>> = defaultVegetationBaseColors,
    vegetationHighlightColors: List<Pair<Int, Color>> = defaultVegetationHighlightColors,
    landscapePoints: List<Float> = defaultLandscapePoints,
    entryContent: @Composable (MoodEntry, Offset, Boolean) -> Unit = { entry, position, isSelected ->
        //DefaultMoodEntry(entry, position, isSelected, rememberTextMeasurer())
        CustomMoodEntry(position, isSelected){
            Icon(
                painter = painterResource(id = R.drawable.baseline_medication_24),
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.inversePrimary,
                contentDescription = null )
        }
    },
    markerContent: @Composable (MoodEntry, Offset) -> Unit = { entry, position ->
        DefaultMoodMarker(entry, position, hourWidthPx)
    }
) {
    val textMeasurer = rememberTextMeasurer()

    // Calculate all mood positions for the connecting line
    val allMoodPositions = moodEntries.map { entry ->
        Pair(entry.time * hourWidthPx, entry.intensity)
    }

    // Draw timeline
    Row(Modifier.horizontalScroll(rememberScrollState())) {
        for (hour in hourRange) {
            val hourEntries = moodEntries.filter {
                it.time >= hour && it.time < hour + 1
            }

            MoodTimeline(
                hour = hour,
                hourWidthPx = hourWidthPx,
                skyColors = skyColors,
                vegetationBaseColors = vegetationBaseColors,
                vegetationHighlightColors = vegetationHighlightColors,
                landscapePoints = landscapePoints,
                moodEntries = hourEntries,
                allMoodPositions = allMoodPositions,
                textMeasurer = textMeasurer,
                entryContent = entryContent,
                markerContent = markerContent
            )
        }
    }
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

// Data class for mood entries
data class MoodEntry(
    val time: Float,         // Time as hour + fraction (e.g., 13.5 for 13:30)
    val emoji: String,       // Emoji representing the mood
    val intensity: Float,    // Used for the connecting line height
    val text: String         // Text message (max 120 chars)
)

// Sample usage example
@Preview(widthDp = 320)
@Composable
fun MoodChartExample() {
    val sampleMoodEntries = listOf(
        MoodEntry(
            time = 8.25f,         // 8:15 AM
            emoji = "😊",
            intensity = 0.8f,
            text = "Feeling great after morning coffee! Ready to tackle the day ahead with energy and focus."
        ),
        MoodEntry(
            time = 10.5f,         // 10:30 AM
            emoji = "🤔",
            intensity = 0.6f,
            text = "Stuck on a challenging problem at work. Trying to figure out the best approach."
        ),
        MoodEntry(
            time = 12.75f,        // 12:45 PM
            emoji = "😋",
            intensity = 0.9f,
            text = "Lunch break! Enjoying my favorite sandwich from the cafe downstairs."
        ),
        MoodEntry(
            time = 14.0f,         // 2:00 PM
            emoji = "😴",
            intensity = 0.4f,
            text = "Post-lunch energy dip. Could really use another coffee right now."
        ),
        MoodEntry(
            time = 16.5f,         // 4:30 PM
            emoji = "😅",
            intensity = 0.7f,
            text = "Meeting went longer than expected. Rushing to finish my tasks before the end of the day."
        ),
        MoodEntry(
            time = 18.25f,        // 6:15 PM
            emoji = "🎉",
            intensity = 0.95f,
            text = "Wrapped up everything! Heading home with a sense of accomplishment. Weekend starts now!"
        ),
        MoodEntry(
            time = 20.0f,         // 8:00 PM
            emoji = "😌",
            intensity = 0.85f,
            text = "Relaxing with a good book and some tea. Perfect evening to unwind."
        ),
        MoodEntry(
            time = 22.5f,         // 10:30 PM
            emoji = "😴",
            intensity = 0.3f,
            text = "Getting sleepy. Time to wrap up and get ready for bed soon."
        )
    )

    // Use default or custom components
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)
    ) {
        MoodChart(
            moodEntries = sampleMoodEntries,
            hourRange = 8..23,
            hourWidthPx = 120f
            // Optional: Use custom entry content
            // entryContent = customEntryContent
        )
    }
}

// Example of completely custom marker content
@Composable
fun CustomMoodMarker(entry: MoodEntry, position: Offset) {
    val hour = position.x.toInt() / 50
    val minutes = ((entry.time - hour) * 60).toInt()
    val timeString = "${hour}:${minutes.toString().padStart(2, '0')}"

    Box(
        modifier = Modifier
            .width(150.dp)
            .padding(top = (position.y + 30).dp)
            .offset(x = (position.x - 75).dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(
                    color = Color(0xFF333333).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = entry.emoji,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = entry.text,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timeString,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // Triangle pointer
        Canvas(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2, 0f)
                lineTo(0f, size.height)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(
                path = path,
                color = Color(0xFF333333).copy(alpha = 0.9f)
            )
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.3f),
                style = Stroke(width = 2f)
            )
        }
    }
}
data class OffsetDp(val x: Dp, val y: Dp)