package com.example.halocare.ui.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface HaloCareDestinations{
    val route : String
    val icon : ImageVector
}

object WelcomeScreen : HaloCareDestinations{
    override val route: String = "welcome_screen"
    override val icon: ImageVector = Icons.Default.KeyboardArrowRight
}
object LoginScreen : HaloCareDestinations{
    override val route: String = "login_screen"
    override val icon: ImageVector = Icons.Default.Refresh
}
object RegisterScreen : HaloCareDestinations{
    override val route: String = "signUp_screen"
    override val icon: ImageVector = Icons.Default.Create
}
object ProfileScreen : HaloCareDestinations{
    override val route: String = "profile_screen"
    override val icon: ImageVector = Icons.Default.AccountCircle
}
object HomeScreen : HaloCareDestinations{
    override val icon: ImageVector = Icons.Default.Create
    override val route: String = "home_screen"
}
object ConsultsScreen : HaloCareDestinations{
    override val route: String = "consults_screen"
    override val icon: ImageVector = Icons.Default.AccountCircle
}
object AppointmentsScreen : HaloCareDestinations{
    override val route: String = "appointments_screen"
    override val icon: ImageVector = Icons.Default.AccountCircle
}
object HealthTrackingScreen : HaloCareDestinations{
    override val route: String = "health_tracking"
    override val icon: ImageVector = Icons.Default.ThumbUp
}
object DailyHabitsScreen : HaloCareDestinations{
    override val route: String = "habits_screen"
    override val icon: ImageVector = Icons.Default.AccountCircle
}
object MoodScreen : HaloCareDestinations{
    override val route: String = "moods_screen"
    override val icon: ImageVector = Icons.Default.AccountCircle
}
object MedicationScreen : HaloCareDestinations{
    override val route: String = "medications_reminder_screen"
    override val icon: ImageVector = Icons.Default.Create
}
object PediatricDevelopmentScreen: HaloCareDestinations{
    override val route: String = "ped_dev_screen"
    override val icon: ImageVector = Icons.Default.Person
}
object TelehealthScreen: HaloCareDestinations{
    override val route: String = "telehealth_screen"
    override val icon: ImageVector = Icons.Default.Person
}
object SettingsScreen: HaloCareDestinations{
    override val route: String = "settings_screen"
    override val icon: ImageVector = Icons.Default.Person
}