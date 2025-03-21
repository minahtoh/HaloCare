package com.example.halocare

import MoodTrackerScreen
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.HaloCareTheme
import com.example.halocare.ui.models.Professional
import com.example.halocare.ui.presentation.AppointmentsScreen
import com.example.halocare.ui.presentation.ConsultsScreen
import com.example.halocare.ui.presentation.DailyHabitsScreen
import com.example.halocare.ui.presentation.HomeScreen
import com.example.halocare.ui.presentation.MedicationReminderScreen
import com.example.halocare.ui.presentation.MedicationScreen
import com.example.halocare.ui.presentation.MoodScreen
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createMedicationsNotificationChannel()

        setContent {
            HaloCareTheme {
                val navHostController = rememberNavController()

                HaloCareNavHost(navHostController = navHostController, modifier = Modifier.fillMaxSize())
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
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun HaloCareNavHost(
    navHostController: NavHostController,
    modifier: Modifier
){
    val dummySpecialties = listOf(
        "Neurology",
        "Pediatrics",
        "Cardiology",
        "Dermatology",
        "Orthopedics"
    )

    val dummyProfessionals = listOf(
        Professional(
            name = "Dr. Alice Smith",
            specialty = "Neurology",
            availableDates = listOf(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3))
        ),
        Professional(
            name = "Dr. John Doe",
            specialty = "Pediatrics",
            availableDates = listOf(LocalDate.now().plusDays(2), LocalDate.now().plusDays(4))
        ),
        Professional(
            name = "Dr. Sarah Johnson",
            specialty = "Cardiology",
            availableDates = listOf(LocalDate.now().plusDays(1), LocalDate.now().plusDays(5))
        ),
        Professional(
            name = "Dr. Michael Brown",
            specialty = "Cardiology",
            availableDates = listOf(LocalDate.now().plusDays(2), LocalDate.now().plusDays(3))
        ),
        Professional(
            name = "Dr. Emily Wilson",
            specialty = "Dermatology",
            availableDates = listOf(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5))
        ),
        Professional(
            name = "Dr. David Anderson",
            specialty = "Orthopedics",
            availableDates = listOf(LocalDate.now().plusDays(1), LocalDate.now().plusDays(4))
        )
    )

    NavHost(navController = navHostController, startDestination = MedicationScreen.route ){
        composable(route = HomeScreen.route){
            HomeScreen()
        }
        composable(route = ConsultsScreen.route){
            ConsultsScreen()
        }
        composable(route = AppointmentsScreen.route){
            AppointmentsScreen(
                availableSpecialties = dummySpecialties,
                availableProfessionals = dummyProfessionals
            )
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
    }
}