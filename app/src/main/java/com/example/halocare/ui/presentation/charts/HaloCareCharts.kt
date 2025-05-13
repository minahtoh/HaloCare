package com.example.halocare.ui.presentation.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halocare.ui.models.ExerciseData
import com.example.halocare.ui.utils.CustomLineTextMarker
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HaloCharts(
    exerciseDataList: List<ExerciseData>,
    featureName:String
) {
    if (exerciseDataList.isEmpty()) {
        Text(
            text = "No data available",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp)
        )
        return
    }


    // Create an Animatable for each column
    val animatables = remember { mutableStateListOf<Animatable<Float, AnimationVector1D>>() }

    LaunchedEffect(exerciseDataList) {
        animatables.clear()
        animatables.addAll(List(exerciseDataList.size) { Animatable(0f) })

        exerciseDataList.forEachIndexed { index, _ ->
            launch {
                delay(index * 100L)
                animatables[index].animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    if (animatables.size != exerciseDataList.size) {
        return
    }

    // Calculate current values based on animation progress
    val currentValues = exerciseDataList.mapIndexed { index, value ->
        value.timeElapsed * animatables[index].value.orZero()
    }

    // Create the entry model
    val columnData = entryModelOf(
        currentValues.mapIndexed { index, value ->
            FloatEntry(index.toFloat(), value)
        }
    )

    val lineColor = MaterialTheme.colorScheme.primary

    val maxTime = exerciseDataList.maxOfOrNull { it.timeElapsed }?.toFloat() ?: 0f


    Column(
        modifier = Modifier
            .fillMaxWidth().height(350.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(7.dp)
            ).padding(end = 25.dp)
    ) {
        Text(
            text = "$featureName",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 15.dp, top = 8.dp)
        )

        Chart(
            chart = lineChart(),
            model = columnData,
            startAxis = startAxis(
                valueFormatter = TimeAxisValueFormatter(maxTime), // Format Y-axis as whole numbers
                maxLabelCount = 5,
            ),
            bottomAxis = bottomAxis(
                valueFormatter = DateAxisValueFormatter(exerciseDataList),
                guideline = null,
                labelSpacing = 3,
                title = "Days",
                tickLength = 10.dp

            ),
            chartScrollSpec = rememberChartScrollSpec(
                isScrollEnabled = true,
            ),
            horizontalLayout = HorizontalLayout.FullWidth(
                startPaddingDp = 0.dp.value,
                endPaddingDp = 5.dp.value
            ),
            marker =
                CustomLineTextMarker(
                    exerciseDataList
                )
            ,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = 1f }
        )
    }
}

class TimeAxisValueFormatter(private val maxTime: Float) : AxisValueFormatter<AxisPosition.Vertical.Start> {
    override fun formatValue(value: Float, chartValues: ChartValues): String {
        return if (maxTime < 3600f) {
            // Convert seconds to minutes
            "${(value / 60).toInt()} min"
        } else {
            // Convert seconds to hours (with 1 decimal precision)
            "${String.format("%.1f", value / 3600)} hr"
        }
    }
}

class DateAxisValueFormatter(
    private val exerciseDataList: List<ExerciseData>
) : AxisValueFormatter<AxisPosition.Horizontal.Bottom> {

    private val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val outputFormat = SimpleDateFormat("EEE", Locale.getDefault())

    override fun formatValue(value: Float, chartValues: ChartValues): String {
        val index = value.toInt().coerceIn(0, exerciseDataList.size - 1)
        val dateStr = exerciseDataList.getOrNull(index)?.exerciseDate

        return try {
            dateStr?.let {
                val date = inputFormat.parse(it)
                date?.let { d -> outputFormat.format(d) } ?: ""
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}

fun Float?.orZero() = this ?: 0f
