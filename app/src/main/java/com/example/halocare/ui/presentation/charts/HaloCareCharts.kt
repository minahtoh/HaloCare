package com.example.halocare.ui.presentation.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    val animatables = remember {
        List(exerciseDataList.size) { Animatable(0f) }
    }

    // Start sequential animation
    LaunchedEffect(Unit) {
        animatables.forEachIndexed { index, animatable ->
            // Start each animation after a delay
            launch {
                delay(index * 200L) // 200ms delay between columns
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
    }

    // Calculate current values based on animation progress
    val currentValues = exerciseDataList.mapIndexed { index, value ->
        value.timeElapsed * animatables[index].value
    }

    // Create the entry model
    val columnData = remember(currentValues) {
        entryModelOf(
            currentValues.mapIndexed { index, value ->
                FloatEntry(index.toFloat(), value)
            }
        )
    }
    val lineColor = MaterialTheme.colorScheme.primary

    val maxTime = exerciseDataList.maxOfOrNull { it.timeElapsed }?.toFloat() ?: 0f


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            text = "$featureName Usage Over Time",
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
                valueFormatter = { value, _ ->
                    val index = value.toInt().coerceIn(0, exerciseDataList.size - 1)
                    exerciseDataList.getOrNull(index)?.exerciseDate ?: ""
                },
                guideline = null,

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

        // Debugging - Show raw data
        exerciseDataList.forEach {
            Text(text = "${it.exerciseDate}: ${it.exerciseName}", fontSize = 12.sp, color = Color.White, modifier = Modifier.padding(15.dp))
        }
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
