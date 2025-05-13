package com.example.halocare.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halocare.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTrackingScreen(onCategoryClick: (String) -> Unit) {
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.inversePrimary

    LaunchedEffect(Unit){
        statusBarController.updateStatusBar(
            color = statusBarColor,
            darkIcons = true
        )
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
            CategoryCard(
                title = "Daily Habits",
                subtitle = "Streak: 5 days",
                color = Color(0xFFFFCC80),
                imageRes = R.drawable.dumbell_icon,
                onClick = { onCategoryClick(DailyHabitsScreen.route) }
            )
            CategoryCard(
                title = "Mood Tracker",
                subtitle = "Last Entry: Happy",
                color = Color(0xFF90CAF9),
                imageRes = R.drawable.depressed_icon,
                onClick = { onCategoryClick(MoodScreen.route) }
            )
            CategoryCard(
                title = "Medication Reminder",
                subtitle = "Next: 8:00 AM",
                color = Color(0xFFA5D6A7),
                imageRes = R.drawable.baseline_medication_24,
                onClick = { onCategoryClick(MedicationScreen.route) }
            )
        }
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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

@Preview(showBackground = true)
@Composable
fun PreviewHealthTrackingScreen() {
    HealthTrackingScreen(onCategoryClick = {})
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
                    HaloCareStatisticsIcon(
                        contentColor = MaterialTheme.colorScheme.primary,
                        size = 30.dp
                    )
                }
            }

        }
    }
}