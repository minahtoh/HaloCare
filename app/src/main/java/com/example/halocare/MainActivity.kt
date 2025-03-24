package com.example.halocare

import MoodTrackerScreen
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.HaloCareTheme
import com.example.halocare.ui.models.Professional
import com.example.halocare.ui.presentation.AppointmentsScreen
import com.example.halocare.ui.presentation.ConsultsScreen
import com.example.halocare.ui.presentation.DailyHabitsScreen
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
import com.example.halocare.ui.presentation.TelehealthScreen
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createMedicationsNotificationChannel()

        setContent {
            HaloCareTheme {
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
                // Detect scroll direction
                LaunchedEffect(scrollState.value) {
                    isBottomBarVisible = scrollState.value <= previousScrollOffset
                    previousScrollOffset = scrollState.value
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar){
                            HaloCareBottomBarCurved(
                                navHostController,
                                isBottomBarVisible
                            )
                        }
                    }
                ) {
                    HaloCareNavHost(
                        navHostController = navHostController,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        scrollState = scrollState
                    )
                }
            }
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
    scrollState: ScrollState
){

    NavHost(navController = navHostController, startDestination = LoginScreen.route ){
        composable(route = LoginScreen.route){
            LoginScreen(
                onSignupClick = {navHostController.navigateSingleTopTo(RegisterScreen.route)},
                onLoginClick = {navHostController.navigateSingleTopTo(HomeScreen.route)}
            )
        }
        composable(route = RegisterScreen.route){
            RegisterScreen()
        }
        composable(route = ProfileScreen.route){
            ProfileScreen()
        }
        composable(route = HomeScreen.route){
            HomeScreen(
                onProfileClick = {navHostController.navigateSingleTopTo(ProfileScreen.route)}
            )
        }
        composable(route = ConsultsScreen.route){
            ConsultsScreen(
                onBackPressed = {navHostController.navigateUp()},
                scrollState = scrollState
            )
        }
        composable(route = AppointmentsScreen.route){
            AppointmentsScreen()
        }
        composable(route = HealthTrackingScreen.route){
            HealthTrackingScreen(onCategoryClick = {
                navHostController.navigateSingleTopTo(it)
            })
        }
        composable(route = DailyHabitsScreen.route){
            DailyHabitsScreen()
        }
        composable(route = MoodScreen.route){
            MoodTrackerScreen()
        }
        composable(route = MedicationScreen.route){
            MedicationReminderScreen()
        }
        composable(route = PediatricDevelopmentScreen.route){
            PediatricTrackerScreen()
        }
        composable(route = TelehealthScreen.route){
            TelehealthScreen()
        }
        composable(route = SettingsScreen.route){
            SettingsScreen()
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String, popBackStack : Boolean = false) =
    this.navigate(route) {
        launchSingleTop = true
        if (popBackStack){popBackStack()}
    }