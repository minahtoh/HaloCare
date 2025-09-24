package com.example.halocare.ui.presentation.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.halocare.ui.models.ScreenTimeEntry
import com.example.halocare.ui.presentation.responsive


@Composable
fun ScreenTimePieChart(
    screenTimeData: List<ScreenTimeEntry>,
    modifier: Modifier = Modifier,
    isDarkMode : Boolean
) {
    val filteredData = screenTimeData.filter { it.minutes > 0 }
    val totalTime = filteredData.sumOf { it.minutes }
    val colors = filteredData.map { it.color }
    val timeText = if (totalTime >= 60) {
        val hours = totalTime / 60
        val minutes = totalTime % 60
        if (minutes == 0) "$hours hr" else "$hours hr $minutes min"
    } else {
        "$totalTime min"
    }

    // Animate sweep angles
    val animatedSweepAngles = remember(filteredData) {
        filteredData.map {
            Animatable(0f)
        }
    }

    LaunchedEffect(filteredData) {
        filteredData.forEachIndexed { index, entry ->
            val targetSweep = (entry.minutes.toFloat() / totalTime) * 360f
            animatedSweepAngles[index].animateTo(
                targetSweep,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
    }
    if (filteredData.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .background(
                    color = if (isDarkMode) MaterialTheme.colorScheme.primaryContainer else
                        MaterialTheme.colorScheme.surfaceTint,
                    shape = RoundedCornerShape(7.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No screen time data available.\nSelect apps to track usage.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
        }
    }else{
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(6.dp)
                .background(
                    color =  MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(7.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Total Screen Time: $timeText",
                style = MaterialTheme.typography.titleMedium.responsive()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(modifier = modifier.size(200.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2.2f
                var startAngle = -90f

                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f),
                    center = center + Offset(4f, 4f),
                    radius = radius + 8f
                )

                animatedSweepAngles.forEachIndexed { index, animSweep ->
                    val color = colors[index]
                    val sweepAngle = animSweep.value

                    drawArc(
                        color = Color.White,
                        startAngle = startAngle - 0.5f,
                        sweepAngle = sweepAngle + 1f,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )

                    startAngle += sweepAngle
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                filteredData.forEach { entry ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(entry.color, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${entry.appName}: ${entry.minutes} min", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}