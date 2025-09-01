package com.example.halocare.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.halocare.ui.presentation.responsive
import com.example.halocare.ui.presentation.responsiveHeight
import com.example.halocare.ui.presentation.responsiveWidth

class FabCutoutShape(
    private val screenWidthDp: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height

        // Scale based on screen width ratio (Pixel 6a reference: 411dp)
        val referenceWidth = 411.dp
        val scaleFactor = (screenWidthDp / referenceWidth).coerceIn(0.8f, 1.2f)

        val fabRadius = with(density) { (50.dp * scaleFactor).toPx() }
        val fabCenterX = width / 2
        val fabBottomY = height - (fabRadius * 4) / 5

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



@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Yes",
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.size(width = 330.dp.responsiveWidth(), height = 250.dp.responsiveHeight()),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.responsive(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Message section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.responsive(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // Button section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(dismissButtonText)
                    }
                    TextButton(onClick = {
                        onConfirm()
                        onDismiss()
                    }) {
                        Text(
                            confirmButtonText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}