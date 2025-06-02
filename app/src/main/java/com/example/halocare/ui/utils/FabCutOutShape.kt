package com.example.halocare.ui.utils

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class FabCutoutShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height

        // Convert dp to pixels with density
        val fabRadius = with(density) { 60.dp.toPx() }
        val fabCenterX = width / 2
        val fabBottomY = height - (fabRadius * 2) / 3

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(fabCenterX - fabRadius * 1.5f, 0f)

            cubicTo(
                fabCenterX - fabRadius, 0f,
                fabCenterX - fabRadius * 0.5f, fabBottomY,
                fabCenterX, fabBottomY
            )
            cubicTo(
                fabCenterX + fabRadius * 0.5f, fabBottomY,
                fabCenterX + fabRadius, 0f,
                fabCenterX + fabRadius * 1.5f, 0f
            )

            lineTo(width, 0f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        return Outline.Generic(path)
    }
}
