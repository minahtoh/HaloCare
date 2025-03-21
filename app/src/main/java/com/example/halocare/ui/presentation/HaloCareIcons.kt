package com.example.halocare.ui.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Preview
@Composable
fun HaloCareHomeIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    size: Dp = 40.dp,
    isSelected: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceTint,
    contentColor: Color = MaterialTheme.colorScheme.secondary
){
    val lineWidth = size / 5
    val strokeWidth = size / 10
    val pushWidth = 2.dp

    Canvas(
        modifier = modifier
            .size(size)
            .clickable { onClick() },
    ){
        if (isSelected) {
            // Draw a filled background shape
            drawCircle(
                color = contentColor,
                radius = (size+10.dp).toPx() / 2
            )
        }
        // Use contrasting color if selected
        val iconColor = if (isSelected) backgroundColor else contentColor

        drawLine(
            start = Offset(center.x - lineWidth.toPx(), center.y + (lineWidth*2).toPx()),
            end = Offset(center.x + lineWidth.toPx(), center.y + (lineWidth*2).toPx()),
            color = iconColor,
            strokeWidth = strokeWidth.toPx()
        )
        drawLine(
            end = Offset(center.x + (lineWidth * 2).toPx(), center.y ),
            start = Offset(center.x + lineWidth.toPx(), center.y + (lineWidth*2).toPx()),
            color = iconColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            end = Offset(center.x - (lineWidth * 2).toPx(), center.y ),
            start = Offset(center.x - lineWidth.toPx(), center.y + (lineWidth*2).toPx()),
            color = iconColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            end = Offset(center.x - (lineWidth * 2).toPx(), center.y ),
            start = Offset(center.x, center.y - (lineWidth * 2).toPx()),
            color = iconColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            end = Offset(center.x + (lineWidth * 2).toPx(), center.y ),
            start = Offset(center.x, center.y - (lineWidth * 2).toPx()),
            color = iconColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            start = Offset(center.x,center.y - (lineWidth).toPx() + pushWidth.toPx()),
            end = Offset(center.x,center.y + (lineWidth).toPx() + pushWidth.toPx()),
            color = iconColor,
            strokeWidth = (strokeWidth*2/3).toPx(),
        )
        drawLine(
            start = Offset(center.x - (lineWidth).toPx(),center.y + pushWidth.toPx() ),
            end = Offset(center.x + (lineWidth).toPx(),center.y + pushWidth.toPx()),
            color = iconColor,
            strokeWidth = (strokeWidth*2/3).toPx(),
        )
    }
}

@Preview
@Composable
fun HaloCareStatisticsIcon(
    modifier: Modifier = Modifier,
    onClick : ()-> Unit = {},
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceTint,
    contentColor: Color = MaterialTheme.colorScheme.secondary
){
    val lineWidth = size - 35.dp
    val strokeWidth = size/16
    val backWidth = size - 30.dp
    val movement = 3.dp
    Canvas(modifier = modifier
        .size(size)
        .clip(CircleShape)
        .clickable {
            onClick()
        }
        //.padding(10.dp)
    ){
        drawLine(
            start = Offset(center.x - backWidth.toPx(), center.y + backWidth.toPx()),
            end = Offset(center.x - backWidth.toPx(), center.y - backWidth.toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round

        )
        drawLine(
            start = Offset(center.x + backWidth.toPx(), center.y + backWidth.toPx()),
            end = Offset(center.x - backWidth.toPx(), center.y + backWidth.toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            start = Offset(center.x + (lineWidth/4).toPx() - movement.toPx(), center.y - (lineWidth).toPx()),
            end = Offset(center.x - (lineWidth/2).toPx() - movement.toPx(), center.y + (lineWidth/4).toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            start = Offset(center.x + (lineWidth/4).toPx() - movement.toPx(), center.y - (lineWidth).toPx()),
            end = Offset(center.x + (lineWidth).toPx() - movement.toPx(), center.y + (lineWidth/4).toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            start = Offset(center.x + (lineWidth * 2 ).toPx() - movement.toPx(), center.y - (lineWidth).toPx()),
            end = Offset(center.x + (lineWidth).toPx() - movement.toPx(), center.y + (lineWidth/4).toPx()),
            color = contentColor,
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
    }
}

