package com.example.halocare.ui.presentation.charts

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathSegment
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halocare.ui.models.SleepData
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.endAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.marker.markerComponent
import com.patrykandpatrick.vico.compose.component.shape.chartShape
import com.patrykandpatrick.vico.compose.component.shape.composeShape
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.composed.plus
import com.patrykandpatrick.vico.core.chart.copy
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer.Companion.composedChartEntryModelOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.marker.Marker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

//@Preview
@Composable
fun SleepTrackerChart(
    sleepData: List<SleepData>,
    modifier: Modifier = Modifier
) {

    val animatables = remember(sleepData) {
        sleepData.map { Animatable(0f) }
    }

    // Start sequential animation
    LaunchedEffect(sleepData) {
        animatables.forEachIndexed { index, animatable ->
            launch {
                delay(index * 200L) // 200ms delay between columns
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
        }
    }
    // Sleep quality emojis (1-5 scale)
    val sleepQualityIcons = listOf("😴", "🙂", "😐", "😕", "😢")

    val columnValues = sleepData.mapIndexed { index, value ->
        value.sleepLength * animatables[index].value
    }

    val columnChartData = remember(columnValues) {
        entryModelOf(
            columnValues.mapIndexed { index, value ->
                FloatEntry(index.toFloat(), value)
            }
        )
    }

    val lineValues = sleepData.mapIndexed { index, value ->
        value.sleepQuality * animatables[index].value
    }

    val lineChartData = remember(lineValues) {
        entryModelOf(
            lineValues.mapIndexed { index, value ->
                FloatEntry(index.toFloat(), value)
            }
        )
    }

    val columnStyle = LineComponent(
        color = MaterialTheme.colorScheme.inversePrimary.toArgb(),
        thicknessDp = 15f, // Column width
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp).chartShape()
    )

    val sleepColumn = columnChart(
        columns = listOf(columnStyle)
    )
    val sleepLine = lineChart()


    val sleepLengthFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        "${value.toInt()}h" // Example: Format as "8h" for hours
    }

    val sleepQualityFormatter = AxisValueFormatter<AxisPosition.Vertical.End> { value, _ ->
        val index = value.toInt().coerceIn(1, 5) - 1 // Sleep quality is 1–5 scale
        sleepQualityIcons.reversed().getOrElse(index) { "❓" }   // Fallback emoji
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondaryContainer
            )
            .padding(16.dp)
    ){
        Text(
            text = "Sleep Habits Over Time",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 15.dp, top = 8.dp)
        )

        Chart(
            chart = sleepColumn + sleepLine,
            model = composedChartEntryModelOf(listOf(columnChartData, lineChartData)),
            startAxis = startAxis(
                valueFormatter = sleepLengthFormatter,
                maxLabelCount = 5
            ),
            endAxis = endAxis(
                valueFormatter = sleepQualityFormatter,
                maxLabelCount = 5
            ),

            bottomAxis = bottomAxis(
                valueFormatter = { value, _ ->
                    val index = value.toInt().coerceIn(0, sleepData.size - 1)
                    val dayString = sleepData.getOrNull(index)?.dayLogged

                    dayString?.let {
                        try {
                            val date = LocalDate.parse(it, DateTimeFormatter.ISO_DATE)
                            date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) // "Mon", "Tue", etc.
                        } catch (e: Exception) {
                            "" //unlikely
                        }
                    } ?: ""
                },
                guideline = null
            ),
            chartScrollSpec = rememberChartScrollSpec(
                isScrollEnabled = true,
            ),
            horizontalLayout = HorizontalLayout.FullWidth(
                startPaddingDp = 0.dp.value,
                endPaddingDp = 0.dp.value
            ),
            marker = CustomSleepMarker(sleepData)
        )
    }
}

class CustomSleepMarker(private val sleepData: List<SleepData>) : Marker {
    private val labelPaint = Paint().apply {
        color = Color.Black.toArgb()
        textSize = 20.sp.value
        textAlign = Paint.Align.CENTER
    }
    private val backgroundPaint = Paint().apply {
        color = Color.White.toArgb()
        style = Paint.Style.FILL
        setShadowLayer(8f, 0f, 4f, Color.Gray.toArgb()) // Add shadow
    }
    private val dashedLinePaint = Paint().apply {
        color = Color.Gray.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) // Dashed Effect
    }

    private val pointPaint = Paint().apply {
        color = Color.Blue.toArgb() // Color of the dot on the line
        style = Paint.Style.FILL
    }

    // Sleep quality emojis (1-5 scale)
    private val sleepQualityIcons = listOf("😴", "🙂", "😐", "😕", "😢")

    override fun draw(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
        chartValuesProvider: ChartValuesProvider
    ) {
        if (markedEntries.isEmpty()) return

        val entry = markedEntries.first()
        val x = entry.location.x
        val y = entry.location.y - 30f // Position above the point
        val dataIndex = entry.index

        val sleepDuration = sleepData[dataIndex].sleepLength
        val sleepQuality = sleepData[dataIndex].sleepQuality.coerceIn(1, 5) // Ensure within valid range
        val sleepEmoji = sleepQualityIcons[sleepQuality - 1] // Get corresponding emoji

        // Draw background for marker
        context.canvas.drawRoundRect(
            x - 50f, y - 40f, x + 50f, y + 10f,
            8f, 8f, backgroundPaint
        )

        // Draw primary text (sleep duration)
        labelPaint.color = Color.Black.toArgb()
        context.canvas.drawText(
            "Slept for ${sleepDuration.roundToLong()}h",
            x, y - 15f, labelPaint
        )

        // Draw emoji for sleep quality
        labelPaint.color = Color.Gray.toArgb()
        context.canvas.drawText(
            "Felt Like $sleepEmoji",
            x, y + 5f, labelPaint
        )

        // Draw dashed guideline to x-axis
        context.canvas.drawLine(x, y + 10f, x, bounds.bottom, dashedLinePaint)

        // Draw the small point on the line chart
        context.canvas.drawCircle(x, entry.location.y, 6f, pointPaint)
    }

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions
    ) {
        outInsets.top = 50f // Adjust for marker placement
    }
}


