package com.example.halocare.ui.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(widthDp = 320, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("General", style = MaterialTheme.typography.titleMedium)
            SettingItem(
                title = "Notifications",
                subtitle = "Enable or disable reminders",
                icon = Icons.Outlined.List
            ) {
                // Toggle notification logic
            }

            SettingItem(
                title = "Dark Mode",
                subtitle = "Switch between light and dark themes",
                icon = Icons.Outlined.Star
            ) {
                // Toggle dark mode logic
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Text("Account", style = MaterialTheme.typography.titleMedium)
            SettingItem(
                title = "Profile Info",
                subtitle = "View and edit your profile",
                icon = Icons.Default.Person
            ) {
                // Navigate to Profile Screen
            }

            SettingItem(
                title = "Logout",
                subtitle = "Sign out of your account",
                icon = Icons.Default.ExitToApp
            ) {
                // Handle logout
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Text("App Info", style = MaterialTheme.typography.titleMedium)
            SettingItem(
                title = "About HaloCare",
                subtitle = "Learn more about the app",
                icon = Icons.Default.Info
            ) {
                // Show About Dialog
            }

            SettingItem(
                title = "Privacy Policy",
                subtitle = "Read our privacy policy",
                icon = Icons.Default.Info
            ) {
                // Open Privacy Policy Link
            }
        }
    }
}

@Composable
fun SettingItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
