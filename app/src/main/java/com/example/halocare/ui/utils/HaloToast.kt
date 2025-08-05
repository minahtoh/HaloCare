package com.example.halocare.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

@Composable
fun HaloCareToast(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.inversePrimary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    durationMillis: Int = 2000
) {
    val visible = remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(durationMillis.toLong())
        visible.value = false
    }

    if (visible.value) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(10f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 6.dp,
                shadowElevation = 3.dp,
                color = backgroundColor,
                modifier = Modifier
                    .wrapContentHeight()
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }
        }
    }
}

class ToastState {
    var message by mutableStateOf<String?>(null)
    var icon: ImageVector? = null

    fun show(message: String, icon: ImageVector? = null) {
        this.message = message
        this.icon = icon
    }

    fun clear() {
        message = null
        icon = null
    }
}

@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}

@Composable
fun ToastHost(toastState: ToastState, modifier: Modifier) {
    val currentMessage = toastState.message

    if (currentMessage != null) {
        HaloCareToast(
            message = currentMessage,
            icon = toastState.icon,
            durationMillis = 2000,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp) // Optional position adjustment
        )

        // Auto-clear after showing
        LaunchedEffect(currentMessage) {
            delay(2000L)
            toastState.clear()
        }
    }
}
