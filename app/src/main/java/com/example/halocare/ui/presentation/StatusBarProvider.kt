package com.example.halocare.ui.presentation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


private val LocalStatusBarController = staticCompositionLocalOf<StatusBarController?> { null }

class StatusBarController(
    private val activity: Activity
) {
    fun updateStatusBar(
        color: Color,
        darkIcons: Boolean = true,
        isVisible: Boolean = true
    ) {
        val window = activity.window
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        window.statusBarColor = color.toArgb()
        controller.isAppearanceLightStatusBars = darkIcons

        if (isVisible) {
            controller.show(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        }
    }
}

@Composable
fun StatusBarProvider(content: @Composable () -> Unit) {
    val view = LocalView.current
    val controller = remember {
        if (!view.isInEditMode) {
            StatusBarController(view.context as Activity)
        } else null
    }

    CompositionLocalProvider(
        LocalStatusBarController provides controller
    ) {
        content()
    }
}

@Composable
fun rememberStatusBarController(): StatusBarController {
    return LocalStatusBarController.current
        ?: error("No StatusBarController found! Did you forget to wrap your content in StatusBarProvider?")
}
