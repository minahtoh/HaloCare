package com.example.halocare.ui.utils

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.sp
import com.example.halocare.ui.models.ExerciseData
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.marker.Marker
import java.lang.Float.max

class CustomLineTextMarker(
    private val markerList: List<ExerciseData>,
    private val dotColor: Color = Color.Blue
) : Marker {

    private val labelPaint = Paint().apply {
        color = Color.Black.toArgb()
        textSize = 16.sp.value
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE // More stylish font
        isAntiAlias = true // Smooth edges
    }

    private val secondaryTextPaint = Paint().apply {
        color = Color.Gray.toArgb()
        textSize = 14.sp.value
        textAlign = Paint.Align.CENTER
        typeface = Typeface.SANS_SERIF
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        color = Color.White.toArgb()
        style = Paint.Style.FILL
        setShadowLayer(10f, 0f, 4f, Color.Black.copy(alpha = 80f).toArgb()) // Smooth shadow
    }

    private val guidelinePaint = Paint().apply {
        color = Color.Gray.copy(alpha = 120f).toArgb()
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) // Dashed effect
    }

    private val dotPaint = Paint().apply {
        color = dotColor.toArgb()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val dotOutlinePaint = Paint().apply {
        color = Color.White.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    override fun draw(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
        chartValuesProvider: ChartValuesProvider
    ) {
        if (markedEntries.isEmpty()) return

        val entry = markedEntries.first()
        val x = entry.location.x
        val y = entry.location.y - 50f // Position above the point
        val dataIndex = entry.index

        val primaryText = "${entry.entry.y.toInt()}"
        val secondaryText = markerList.getOrNull(dataIndex)?.exerciseName ?: "N/A"

        val timeInSeconds = markerList[dataIndex].timeElapsed

        val formattedTime = if (timeInSeconds >= 3600) {
            val hours = (timeInSeconds / 3600).toInt()
            val minutes = ((timeInSeconds % 3600) / 60).toInt()
            val seconds = (timeInSeconds % 60).toInt()
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            val minutes = (timeInSeconds / 60).toInt()
            val seconds = (timeInSeconds % 60).toInt()
            String.format("%02d:%02d", minutes, seconds)
        }

        // Dynamic background width
        val textWidth = max(
            labelPaint.measureText(primaryText),
            secondaryTextPaint.measureText(secondaryText)
        ) + 40f

        val bgLeft = x - textWidth / 2
        val bgTop = y - 45f
        val bgRight = x + textWidth / 2
        val bgBottom = y + 15f

        // Draw background
        context.canvas.drawRoundRect(
            bgLeft, bgTop, bgRight, bgBottom,
            16f, 16f, backgroundPaint
        )

        // Draw primary text (value)
        context.canvas.drawText(
            formattedTime,
            x, y - 10f, labelPaint
        )

        // Draw secondary text (exercise name)
        context.canvas.drawText(
            secondaryText,
            x, y + 10f, secondaryTextPaint
        )

        // Draw guideline (optional)
        context.canvas.drawLine(
            x, bgBottom, x, entry.location.y,
            Paint().apply {
                color = Color.Gray.copy(alpha = 120f).toArgb()
                strokeWidth = 2f
                pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) // Dashed effect
            }
        )
        // Extend dashed guideline from chart down to the X-axis
        context.canvas.drawLine(
            x, entry.location.y, x, bounds.bottom - 20f, // Stops above X-axis
            guidelinePaint
        )

        // **NEW: Draw dot at data point**
        context.canvas.drawCircle(x, entry.location.y, 8f, dotOutlinePaint) // Outer white stroke
        context.canvas.drawCircle(x, entry.location.y, 6f, dotPaint) // Inner colored dot
    }

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions
    ) {
        outInsets.top = 50f // Ensure space for marker
    }
}
