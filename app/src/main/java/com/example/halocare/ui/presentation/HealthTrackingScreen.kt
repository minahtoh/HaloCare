package com.example.halocare.ui.presentation

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halocare.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTrackingScreen(
    onCategoryClick: (String) -> Unit,
    isDarkMode: Boolean
) {
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.inversePrimary

    val darkTheme = isDarkMode

    LaunchedEffect(Unit){
        statusBarController.updateStatusBar(
            color = statusBarColor,
            darkIcons = true
        )
        Log.d("HEALTH TRACKING", "HealthTrackingScreen: is it darkmode $darkTheme")
    }
    Scaffold(
        topBar = {
            HealthTrackingTopBar()
        },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)) {

            // Force recomposition when theme changes

            // Alternative: Use MaterialTheme colors directly (recommended)
            val habitColor = if (darkTheme) Color(0xFF8D6E63) else Color(0xFFFFCC80)
            val moodColor = if (darkTheme) Color(0xFF5C6BC0) else Color(0xFF90CAF9)
            val medicationColor = if (darkTheme) Color(0xFF689F38) else Color(0xFFA5D6A7)
            val devColor = if (darkTheme) Color(0xFF8E24AA) else Color(0xFFE1BEE7)

            CategoryCard(
                title = "Daily Habits",
                subtitle = "Streak: 5 days",
                color = habitColor,
                imageRes = R.drawable.dumbell_icon,
                onClick = { onCategoryClick(DailyHabitsScreen.route) }
            )

            CategoryCard(
                title = "Mood Tracker",
                subtitle = "Last Entry: Happy",
                color = moodColor,
                imageRes = R.drawable.depressed_icon,
                onClick = { onCategoryClick(MoodScreen.route) }
            )

            CategoryCard(
                title = "Medication Reminder",
                subtitle = "Next: 8:00 AM",
                color = medicationColor,
                imageRes = R.drawable.baseline_medication_24,
                onClick = { onCategoryClick(MedicationScreen.route) }
            )

            CategoryCard(
                title = "Pediatric Dev Tracker",
                subtitle = "Age: 18 months",
                color = devColor,
                imageRes = R.drawable.ped_dev_tracker_ic,
                onClick = { onCategoryClick(PediatricDevelopmentScreen.route) }
            )
        }
    }
}

@Composable
fun rememberIsDarkTheme(): Boolean {
    val configuration = LocalConfiguration.current
    return remember(configuration.uiMode) {
        (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}

@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    color: Color,
    imageRes : Int,
    onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, fontSize = 16.sp, color = Color.DarkGray)
            }
        }
    }
}



@Composable
fun HealthTrackingTopBar(){
    Surface(
        color = MaterialTheme.colorScheme.inversePrimary,
        shadowElevation = 7.dp,
        modifier = Modifier.fillMaxWidth()
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Health Tracking",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.surfaceTint,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                shadowElevation = 5.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = R.drawable.health_tracking_ic),
                        contentDescription = null )
                }
            }

        }
    }
}