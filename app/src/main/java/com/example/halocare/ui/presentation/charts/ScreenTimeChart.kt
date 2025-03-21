package com.example.halocare.ui.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.halocare.ui.models.ScreenTimeEntry

@Preview
@Composable
fun ScreenTimePieChart(
  //  screenTimeData: List<ScreenTimeEntry>,
    modifier: Modifier = Modifier)
{
    val sampleScreenTime = listOf(
        ScreenTimeEntry("WhatsApp", 120, Color(0xFFE57373)),  // Red
        ScreenTimeEntry("YouTube", 90, Color(0xFF81C784)),    // Green
        ScreenTimeEntry("Instagram", 60, Color(0xFF64B5F6)), // Blue
        ScreenTimeEntry("Facebook", 30, Color(0xFFFFD54F)),  // Yellow
        ScreenTimeEntry("Twitter", 20, Color(0xFFBA68C8)),   // Purple
        ScreenTimeEntry("Reddit", 10, Color(0xFFFF8A65)),    // Orange
        ScreenTimeEntry("Snapchat", 0, Color(0xFF4DB6AC))    // **Will not appear** (0 minutes)
    )
    val filteredData = sampleScreenTime.filter { it.minutes > 0 } // Remove apps with 0 minutes
    val totalTime = filteredData.sumOf { it.minutes }
    val sweepAngles = filteredData.map { (it.minutes.toFloat() / totalTime) * 360f }
    val colors = filteredData.map { it.color }


    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Total Screen Time: $totalTime min",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(modifier = modifier.size(200.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.2f
            var startAngle = -90f

            // Draw shadow for elevation effect
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                center = center + Offset(4f, 4f), // Offset for shadow effect
                radius = radius + 8f // Slightly larger shadow radius
            )

            sweepAngles.forEachIndexed { index, sweepAngle ->
                val color = colors[index]

                // Draw white separator
                drawArc(
                    color = Color.White,
                    startAngle = startAngle - 0.5f, // Adjusted for equal spacing
                    sweepAngle = sweepAngle + 1f, // More precise spacing
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )

                // Draw actual pie slice
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

        // Legend
        Column {
            filteredData.forEach { entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(entry.color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${entry.appName}: ${entry.minutes} min",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}