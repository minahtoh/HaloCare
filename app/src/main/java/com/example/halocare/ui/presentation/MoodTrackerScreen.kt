import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.halocare.R
import com.example.halocare.ui.presentation.charts.MoodChartExample
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

@Preview(widthDp = 320, heightDp = 720)
@Composable
fun DemoMoodTrackerScreen() {
    val moods = remember { mutableStateListOf(
        MoodEntry("😊", "Feeling good!", 6),
        MoodEntry("😞", "Tired and drained", 12),
        MoodEntry("😡", "Frustrated at work", 18),
        MoodEntry("😁", "Excited for the weekend!", 21)
    ) }

    var selectedMood by remember { mutableStateOf<MoodEntry?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Mood Tracker", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

        HorizontalScrollContainer(moods = moods)

        AnimatedVisibility(visible = selectedMood != null) {
            selectedMood?.let {
                MoodDetailBubble(it)
            }
        }
    }
}


@Composable
fun HorizontalScrollContainer(
    modifier: Modifier = Modifier,
    initialSegment: Int = 0,
    moods: List<MoodEntry>
) {
    val lazyListState = rememberLazyListState(initialSegment)

    Box(modifier = modifier
        .fillMaxWidth()
        .height(300.dp)) {
        LazyRow(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(24 / 6) { segment ->
                TimeSegment(
                    startHour = segment * 6,
                    moods = moods
                )
            }
        }
    }
}

@Composable
fun TimeSegment(startHour: Int, moods : List<MoodEntry>) {
    // Sky gradient based on time of day
    val skyGradient = when (startHour) {
        0 -> Brush.verticalGradient(listOf(
            Color(0xFF0A1929), // Deep night
            Color(0xFF162447)  // Early dawn
        ))
        6 -> Brush.verticalGradient(listOf(
            Color(0xFF162447),  // Dawn
            Color(0xFF5B89A6),  // Morning
            Color(0xFFFFD700)   // Midday
        ))
        12 -> Brush.verticalGradient(listOf(
            Color(0xFFFFD700),  // Midday
            Color(0xFFFF9E43),  // Afternoon
            Color(0xFFE4C580)   // Evening
        ))
        18 -> Brush.verticalGradient(listOf(
            Color(0xFFE4C580),  // Evening
            Color(0xFF614C88),  // Dusk
            Color(0xFF0A1929)   // Night
        ))
        else -> Brush.verticalGradient(listOf(Color.Black, Color.Gray))
    }

    // Vegetation colors based on time of day
    val vegetationBaseColor = when (startHour) {
        0 -> Color(0xFF0A2A0A)  // Dark green for night
        6 -> Color(0xFF1B4B1B)  // Medium green for morning
        12 -> Color(0xFF2E6E2E) // Bright green for midday
        18 -> Color(0xFF143814) // Dark green for evening
        else -> Color(0xFF143814)
    }

    val vegetationHighlightColor = when (startHour) {
        0 -> Color(0xFF143814)  // Slightly lighter green for night
        6 -> Color(0xFF2E6E2E)  // Brighter green for morning
        12 -> Color(0xFF3B8F3B) // Brightest green for midday
        18 -> Color(0xFF1B4B1B) // Medium green for evening
        else -> Color(0xFF1B4B1B)
    }

    Canvas(
        modifier = Modifier
            .width(300.dp)
            .height(300.dp)
            .background(skyGradient)
    ) {
        val width = size.width
        val height = size.height
        val hourSpacing = width / 6
        val moodPositions = moods.filter { it.hour in startHour until startHour + 6 }
            .map { it.hour to Offset((it.hour - startHour) * hourSpacing, height / 2) }


        // Draw vegetation at the bottom
        val vegetationHeight = height * 0.3f // Bottom 30% is vegetation
        val vegetationPath = Path().apply {
            moveTo(0f, height - vegetationHeight)
            lineTo(width, height - vegetationHeight)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        // Create a gradient for the vegetation that's appropriate for the time of day
        val vegetationGradient = Brush.verticalGradient(
            colors = listOf(vegetationHighlightColor, vegetationBaseColor),
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
            color = vegetationBaseColor.copy(alpha = 0.7f)
        )

        // Draw hour lines
        for (i in 0..6) {
            val x = i * hourSpacing
            val hour = (startHour + i) % 24

            // Draw vertical line
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(x, 0f),
                end = Offset(x, height-vegetationHeight),
                strokeWidth = 2f
            )

            // Draw hour text with better visibility
            drawIntoCanvas { canvas ->
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 36f
                    textAlign = android.graphics.Paint.Align.CENTER
                    setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                }

                // Draw hour text at the top
                canvas.nativeCanvas.drawText(
                    "${hour}:00",
                    x,
                    50f,
                    textPaint
                )
            }
        }
        moodPositions.zipWithNext { a, b ->
            drawLine(color = Color.Red, start = a.second, end = b.second, strokeWidth = 4f)
        }

        moodPositions.forEach { (_, pos) ->
            drawCircle(color = Color.Yellow, center = pos, radius = 10f)
        }
    }
}
@Composable
fun MoodDetailBubble(mood: MoodEntry) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color.White)) {
            Text(mood.note, fontSize = 16.sp, modifier = Modifier.padding(12.dp))
        }
    }
}

data class MoodEntry(val emoji: String, val note: String, val hour: Int)

@Composable
fun ClaudeHorizontalScrollContainer(
    modifier: Modifier = Modifier,
    initialSegment: Int = 0
) {
    val lazyListState = rememberLazyListState(initialSegment)

    // Define sky colors for the full 24-hour cycle
    val skyColors = listOf(
        0 to Color(0xFF0A1929),  // 00:00 Deep night
        4 to Color(0xFF162447),  // 04:00 Early dawn
        7 to Color(0xFF5B89A6),  // 07:00 Morning
        12 to Color(0xFFFFD700), // 12:00 Midday
        16 to Color(0xFFFF9E43), // 16:00 Afternoon
        19 to Color(0xFFE4C580), // 19:00 Evening
        21 to Color(0xFF614C88), // 21:00 Dusk
        24 to Color(0xFF0A1929)  // 24:00 Night again (full circle)
    )

    // Define vegetation colors for the full 24-hour cycle
    val vegetationBaseColors = listOf(
        0 to Color(0xFF0A2A0A),  // 00:00 Dark green for night
        6 to Color(0xFF1B4B1B),  // 06:00 Medium green for morning
        12 to Color(0xFF2E6E2E), // 12:00 Bright green for midday
        18 to Color(0xFF143814), // 18:00 Dark green for evening
        24 to Color(0xFF0A2A0A)  // 24:00 Dark green again (full circle)
    )

    val vegetationHighlightColors = listOf(
        0 to Color(0xFF143814),  // 00:00 Slightly lighter green for night
        6 to Color(0xFF2E6E2E),  // 06:00 Brighter green for morning
        12 to Color(0xFF3B8F3B), // 12:00 Brightest green for midday
        18 to Color(0xFF1B4B1B), // 18:00 Medium green for evening
        24 to Color(0xFF143814)  // 24:00 Slightly lighter green again (full circle)
    )

    Box(modifier = modifier
        .fillMaxWidth()
        .height(300.dp)) {
        LazyRow(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            // Create individual segments, but with matched colors at boundaries
            items(24) { hour ->
                TimeSegmentHour(
                    hour = hour,
                    skyColors = skyColors,
                    vegetationBaseColors = vegetationBaseColors,
                    vegetationHighlightColors = vegetationHighlightColors
                )
            }
        }
    }
}


// Usage example
@Preview(widthDp = 320)
@Composable
fun TimeChartExample() {
    ClaudeHorizontalScrollContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        initialSegment = 6 // Start at 6 AM
    )
}


@Composable
fun TimeSegmentHour(
    hour: Int,
    skyColors: List<Pair<Int, Color>>,
    vegetationBaseColors: List<Pair<Int, Color>>,
    vegetationHighlightColors: List<Pair<Int, Color>>,
) {
    // Find colors for this exact hour by interpolating from the defined color stops
    fun getInterpolatedColor(colorStops: List<Pair<Int, Color>>, hour: Int): Color {
        // Find the two color stops that this hour falls between
        val beforeStop = colorStops.lastOrNull { it.first <= hour } ?: colorStops.first()
        val afterStop = colorStops.firstOrNull { it.first > hour } ?: colorStops.last()

        // If at exact color stop, return that color
        if (beforeStop.first == hour) return beforeStop.second
        if (afterStop.first == hour) return afterStop.second

        // Calculate how far between stops this hour is (0.0 - 1.0)
        val range = afterStop.first - beforeStop.first
        val position = (hour - beforeStop.first).toFloat() / range

        // Interpolate between colors
        return lerp(beforeStop.second, afterStop.second, position)
    }

    // Get exact colors for this hour
    val skyColor = getInterpolatedColor(skyColors, hour)
    val nextSkyColor = getInterpolatedColor(skyColors, (hour + 1) % 24)
    val vegetationBase = getInterpolatedColor(vegetationBaseColors, hour)
    val vegetationHighlight = getInterpolatedColor(vegetationHighlightColors, hour)

    // Create gradient for this hour that will smoothly transition to next hour
    val skyGradient = Brush.horizontalGradient(listOf(skyColor, nextSkyColor))

    Canvas(
        modifier = Modifier
            .width(50.dp) // Each hour is 50dp wide
            .height(300.dp)
            .background(skyGradient)
    ) {
        val width = size.width
        val height = size.height

        // Draw vegetation at the bottom
        val vegetationHeight = height * 0.3f // Bottom 30% is vegetation
        val vegetationPath = Path().apply {
            moveTo(0f, height - vegetationHeight)
            lineTo(width, height - vegetationHeight)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        // Create a gradient for the vegetation
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

        // Draw hour marker line
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(0f, 0f),
            end = Offset(0f, height-vegetationHeight),
            strokeWidth = 2f
        )

        // Draw hour text
        drawIntoCanvas { canvas ->
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 30f
                textAlign = android.graphics.Paint.Align.CENTER
                setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
            }

            // Draw hour text at the top
            canvas.nativeCanvas.drawText(
                "${hour}:00",
                width / 2,
                50f,
                textPaint
            )
        }
    }
}


@Preview(widthDp = 320, heightDp = 720)
@Composable
fun MoodTrackerScreen(){
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showMoodDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Track whether the user has scrolled down
    val isTopBarVisible by remember {
        derivedStateOf { scrollState.value < 50 }  // Adjust threshold as needed
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showMoodDialog = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_draw_24),
                    contentDescription = "Log Mood",
                    tint = Color.White)
            }
        },
        topBar = {
            AnimatedVisibility(
                visible = isTopBarVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
            ) {
                MoodTrackerTopBar()
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(scrollState)
        ) {
            // Date Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                WeekDateSelector(
                    selectedDate = selectedDate,
                    // onDateSelected = { selectedDate = it }
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
            // Mood Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                MoodChartExample()
            }

            // Mood Insights Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Mood Insights",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.emoji_adviser),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .rotate(360f),
                            tint = MaterialTheme.colorScheme.inversePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("You've logged 'Happy' 5 times this week! Keep it up!", fontSize = 14.sp)
                }
            }

            // Inspirational Quote Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "'Every day may not be good, but there's something good in every day.'",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic)
                }
            }

            if (showMoodDialog) {
                Dialog(onDismissRequest = { showMoodDialog = false }) {
                    MoodEntryLogger()
                }
            }

        }
    }
}


@Preview
@Composable
fun WeekDateSelector(
    selectedDate: LocalDate = LocalDate.now(),
   // onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = remember(selectedDate) {
        val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)
        (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        contentPadding = PaddingValues()
    ) {
        items(daysOfWeek) { date ->
            val isSelected = date == selectedDate
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        //   onDateSelected(date)
                    }
                    .background(if (isSelected) MaterialTheme.colorScheme.errorContainer
                                else Color.Transparent
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()),
                    color = if (isSelected) Color.White else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = date.dayOfMonth.toString(),
                    color = if (isSelected) Color.White else Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun MoodEntryLogger() {
    // State for the mood entry form
    var selectedEmoji by remember { mutableStateOf("😊") }
    var entryText by remember { mutableStateOf("") }
    val maxCharacters = 80

    // Available emoji options
    val emojiOptions = listOf("😊", "😃", "😍", "🙂", "😐", "😕", "😢", "😡", "😴", "🤔")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Emoji selector using ExposedDropdownMenuBox
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedEmoji,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    label = { Text("Select Mood") },
                    trailingIcon = {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select emoji"
                        )
                    }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    emojiOptions.forEach { emoji ->
                        DropdownMenuItem(
                            text = { Text(emoji, fontSize = 24.sp) },
                            onClick = {
                                selectedEmoji = emoji
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text input for mood description with character limit
            OutlinedTextField(
                value = entryText,
                onValueChange = {
                    if (it.length <= maxCharacters) entryText = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Wassup...") },
                label = { Text("Gist") },
                minLines = 2,
                maxLines = 3,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${entryText.length}/$maxCharacters",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button
            Button(
                onClick = {
                    if (entryText.isNotBlank()) {
                        // Reset form after submission
                        entryText = ""
                        selectedEmoji = "😊"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                enabled = entryText.isNotBlank()
            ) {
                Text("Log Mood")
            }
        }
    }
}


@Composable
fun MoodTrackerTopBar(){
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier,
        shadowElevation = 7.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp, start = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50),
                shadowElevation = 5.dp,
                modifier = Modifier.size(30.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_keyboard_double_arrow_right_24),
                    contentDescription = "back",
                    modifier = Modifier.rotate(180f),
                    colorFilter = ColorFilter.tint(
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                )
            }
            Text(
                text = "Mood Tracker",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.surfaceTint,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                shadowElevation = 5.dp,
                modifier = Modifier.size(30.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_psychology_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    }
}