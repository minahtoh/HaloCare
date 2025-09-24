package com.example.halocare

import MoodTrackerScreen
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.HaloCareTheme
import com.example.halocare.ui.presentation.AppointmentsScreen
import com.example.halocare.ui.presentation.ConsultsScreen
import com.example.halocare.ui.presentation.DailyHabitsScreen
import com.example.halocare.ui.presentation.FeatureGridPopup
import com.example.halocare.ui.presentation.HaloCareBottomBarCurved
import com.example.halocare.ui.presentation.HealthTrackingScreen
import com.example.halocare.ui.presentation.HomeScreen
import com.example.halocare.ui.presentation.LoginScreen
import com.example.halocare.ui.presentation.MedicationReminderScreen
import com.example.halocare.ui.presentation.MedicationScreen
import com.example.halocare.ui.presentation.MoodScreen
import com.example.halocare.ui.presentation.PediatricDevelopmentScreen
import com.example.halocare.ui.presentation.PediatricTrackerScreen
import com.example.halocare.ui.presentation.ProfileScreen
import com.example.halocare.ui.presentation.RegisterScreen
import com.example.halocare.ui.presentation.SettingsScreen
import com.example.halocare.ui.presentation.StatusBarProvider
import com.example.halocare.ui.presentation.TelehealthScreen
import com.example.halocare.ui.presentation.rememberStatusBarController
import com.example.halocare.ui.presentation.responsiveHeight
import com.example.halocare.viewmodel.AuthViewModel
import com.example.halocare.viewmodel.MainViewModel
import com.example.halocare.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createMedicationsNotificationChannel()

        val authViewModel: AuthViewModel by viewModels()
        val mainViewModel: MainViewModel by viewModels()
        val settingsViewModel: SettingsViewModel by viewModels()
        val splashScreen = installSplashScreen()
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }


        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Create fade out animation for system splash
            val fadeOut = ObjectAnimator.ofFloat(splashScreenView.view, "alpha", 1f, 0f)
            fadeOut.duration = 300L
            fadeOut.doOnEnd {
                splashScreenView.remove()
            }
            fadeOut.start()
        }

        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            var showCustomSplash by remember { mutableStateOf(true) }

            HaloCareTheme(darkTheme = isDarkMode) {
                if (showCustomSplash) {
                    CustomSplashContent(
                        onSplashComplete = {
                            showCustomSplash = false
                            isReady = true
                        }
                    )
                } else {
                    MainAppContent(
                        settingsViewModel = settingsViewModel,
                        authViewModel = authViewModel,
                        mainViewModel = mainViewModel,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
        lifecycleScope.launch {
            delay(500) // Very short delay
            isReady = true
        }
    }


    private fun createMedicationsNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Medication Reminders"
            val descriptionText = "Reminders for scheduled medications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("medication_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}


@Composable
fun HaloCareNavHost(
    navHostController: NavHostController,
    modifier: Modifier,
    scrollState: ScrollState,
    authViewModel: AuthViewModel,
    mainViewModel : MainViewModel,
    settingsViewModel: SettingsViewModel,
    isDarkMode: Boolean
){

    StatusBarProvider {
        NavHost(navController = navHostController, startDestination = LoginScreen.route) {

            composable(route = LoginScreen.route) {
                LoginScreen(
                    onSignupClick = { navHostController.navigateSingleTopTo(RegisterScreen.route) },
                    onSuccessfulLogin = {
                        navHostController.navigateSingleTopTo(HomeScreen.route, true)
                    },
                    viewModel = authViewModel,
                    isDarkMode = isDarkMode
                )
            }
            composable(route = RegisterScreen.route) {
                RegisterScreen(
                    onSignUpSuccess = {
                        navHostController.navigateSingleTopTo(
                            LoginScreen.route,
                            true
                        )
                    },
                    authViewModel = authViewModel,
                    onLoginClick = { navHostController.navigateSingleTopTo(LoginScreen.route) },
                    isDarkMode = isDarkMode
                )
            }
            composable(route = ProfileScreen.route) {
                ProfileScreen(
                    onSkip = { navHostController.navigateSingleTopTo(HomeScreen.route, true) },
                    onContinue = { navHostController.navigateSingleTopTo(HomeScreen.route) },
                    authViewModel = authViewModel,
                    isDarkMode = isDarkMode
                )
            }
            composable(route = HomeScreen.route) {
                HomeScreen(
                    onProfileClick = { navHostController.navigateSingleTopTo(ProfileScreen.route) },
                    onCategoryClick = {
                        navHostController.navigateSingleTopTo(
                            TelehealthScreen.route
                        )
                    },
                    authViewModel = authViewModel,
                    scrollState = scrollState,
                    mainViewModel = mainViewModel,
                    isDarkMode = isDarkMode
                )
            }
            composable(route = ConsultsScreen.route) {
                ConsultsScreen(
                    onBackPressed = { navHostController.navigateUp() },
                    scrollState = scrollState,
                    onAppointmentsClick = {
                        navHostController.navigateSingleTopTo(
                            AppointmentsScreen.route
                        )
                    },
                    mainViewModel = mainViewModel,
                    isDarkMode = isDarkMode
                )
            }
            composable(route = AppointmentsScreen.route) {
                AppointmentsScreen(
                    mainViewModel = mainViewModel,
                    navigateToConsultsScreen = {
                        navHostController.navigate(ConsultsScreen.route) {
                            popUpTo(AppointmentsScreen.route) {
                                inclusive = true
                            }
                        }
                    },
                    isDarkMode = isDarkMode
                )
            }
            composable(route = HealthTrackingScreen.route) {
                HealthTrackingScreen(
                    onCategoryClick = {
                    navHostController.navigateSingleTopTo(it)
                    },
                    isDarkMode = isDarkMode
                )
            }
            composable(route = DailyHabitsScreen.route) {
                DailyHabitsScreen(
                    mainViewModel = mainViewModel,
                    onBackIconClick = { navHostController.navigateUp() },
                    isDarkMode = isDarkMode
                )
            }
            composable(route = MoodScreen.route) {
                MoodTrackerScreen(
                    mainViewModel = mainViewModel,
                    onBackIconClick = { navHostController.popBackStack() },
                    isDarkMode = isDarkMode
                )
            }
            composable(route = MedicationScreen.route) {
                MedicationReminderScreen(
                    mainViewModel = mainViewModel,
                    onBackIconClick = { navHostController.navigateUp() },
                    isDarkMode = isDarkMode
                )
            }
            composable(route = PediatricDevelopmentScreen.route) {
                PediatricTrackerScreen(
                    onBackIconClick = { navHostController.navigateUp() }
                )
            }
            composable(route = TelehealthScreen.route) {
                TelehealthScreen()
            }
            composable(route = SettingsScreen.route) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    authViewModel = authViewModel,
                    onLogout = {
                        navHostController.navigateSingleTopTo(LoginScreen.route,true)
                    },
                    onProfileClick = {navHostController.navigateSingleTopTo(ProfileScreen.route)}
                )
            }
        }
    }
}


@Composable
fun FeatureGridOverlay(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onFeatureClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(2f) // Make sure it's on top
    ) {
        // Dim background
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )
        }

        // Animated popup card
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 165.dp.responsiveHeight())
        ) {
            FeatureGridPopup(
                onFeatureClick = onFeatureClick
            )
        }
    }
}


// CustomSplashContent.kt - Your fade animation with debugging
@Composable
fun CustomSplashContent(
    onSplashComplete: () -> Unit
) {
    var animationPhase by remember { mutableStateOf(0) }
    // Make splash full screen by hiding system bars
    val context = LocalContext.current
    val activity = context as? Activity

    var shouldComplete by remember { mutableStateOf(false) }

    LaunchedEffect(shouldComplete) {
        if (shouldComplete) {
            delay(50) // Small delay to ensure restoration takes effect
            onSplashComplete()
        }
    }


    DisposableEffect(Unit) {
        activity?.let { act ->
            val window = act.window
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

            // Store originals
            val originalStatusBarColor = window.statusBarColor
            val originalNavigationBarColor = window.navigationBarColor

            // Apply edge-to-edge for splash
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }

        onDispose {
            activity?.let { act ->
                // Restore immediately
                WindowCompat.setDecorFitsSystemWindows(act.window, true)
                val windowInsetsController = WindowCompat.getInsetsController(act.window, act.window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

                //act.window.statusBarColor = window.statusBarColor
                act.window.navigationBarColor = android.graphics.Color.BLACK

                // Trigger delayed completion
                shouldComplete = true
            }
        }
    }


    val firstIconAlpha by animateFloatAsState(
        targetValue = when (animationPhase) {
            0 -> 1f
            1 -> 0f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = 800,
            easing = EaseInOut
        ),
        label = "first_icon_alpha"
    )

    val secondIconAlpha by animateFloatAsState(
        targetValue = when (animationPhase) {
            0, 1 -> 0f
            2 -> 1f
            else -> 1f
        },
        animationSpec = tween(
            durationMillis = 800,
            easing = EaseInOut
        ),
        finishedListener = { alpha ->
            if (animationPhase == 2 && alpha == 1f) {
                // Second icon fade in complete, finish splash
                animationPhase = 3
            }
        },
        label = "second_icon_alpha"
    )

    // Control animation sequence
    LaunchedEffect(Unit) {

        delay(1500) // Show first icon longer
        animationPhase = 1 // Start fade out first icon
        delay(400) // Small overlap
        animationPhase = 2 // Start fade in second icon
        delay(1200) // Show second icon

        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Add a colored background to make sure custom splash is visible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0XFF171D1E)) // Temporary - remove this later
        )

        // First icon
        Icon(
            painter = painterResource(id = R.drawable.halocare_ic_nt),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .alpha(firstIconAlpha),
            tint = Color.Unspecified
        )

        // Second icon
        Icon(
            painter = painterResource(id = R.drawable.halocare_ic_wt),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .alpha(secondIconAlpha),
            tint = Color.Unspecified
        )
    }
}


// MainAppContent.kt - Extract your main app logic
@Composable
fun MainAppContent(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    isDarkMode : Boolean
) {
    val navHostController = rememberNavController()
    val currentRoute = navHostController.currentBackStackEntryAsState().value?.destination?.route

    val showBottomBar = currentRoute in listOf(
        HomeScreen.route,
        ConsultsScreen.route,
        HealthTrackingScreen.route,
        SettingsScreen.route
    )

    var isBottomBarVisible by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    var previousScrollOffset by remember { mutableStateOf(0) }
    var showFeatureGrid by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value) {
        isBottomBarVisible = scrollState.value <= previousScrollOffset
        previousScrollOffset = scrollState.value
    }

    Scaffold(
        // Handle status bar insets for top app bars
        modifier = Modifier
            .fillMaxSize(),
             // This handles status bar for your top app bars
        bottomBar = {
            if (showBottomBar) {
                // Bottom bar with navigation bar insets
                Box(
                    modifier = Modifier.wrapContentHeight()
                ) {
                    HaloCareBottomBarCurved(
                        navHostController,
                        isBottomBarVisible,
                        onFabClick = { showFeatureGrid = !showFeatureGrid }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (!isBottomBarVisible) {
            showFeatureGrid = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showFeatureGrid = false }
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showFeatureGrid = false },
                        onPress = { showFeatureGrid = false }
                    )
                }
        ) {
            HaloCareNavHost(
                navHostController = navHostController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                scrollState = scrollState,
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                settingsViewModel = settingsViewModel,
                isDarkMode = isDarkMode
            )

            FeatureGridOverlay(
                isVisible = showFeatureGrid,
                onDismissRequest = { showFeatureGrid = false },
                onFeatureClick = { route ->
                    showFeatureGrid = false
                    navHostController.navigate(route)
                }
            )
        }
    }
}
@Composable
fun SetNavigationBarColor(color: Color) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(color) {
        activity?.let { act ->
            act.window.navigationBarColor = color.toArgb()
        }
        onDispose { }
    }
}


fun NavHostController.navigateSingleTopTo(
    route: String,
    clearBackStack: Boolean = false
) {
    this.navigate(route) {
        launchSingleTop = true
        restoreState = !clearBackStack

        if (clearBackStack) {
            popUpTo(graph.id) { inclusive = true }
        } else {
            // Normal bottom bar behavior
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
        }
    }
}


fun NavHostController.navigateToHomeScreen(userName:String) =
    this.navigateSingleTopTo(
        route = "${HomeScreen.route}/${userName}",
      //  popBackStack = true
    )

