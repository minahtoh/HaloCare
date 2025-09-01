package com.example.halocare.ui.presentation

import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.halocare.R
import com.example.halocare.ui.utils.ConfirmActionDialog
import com.example.halocare.viewmodel.AuthUiState
import com.example.halocare.viewmodel.AuthViewModel
import com.example.halocare.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

//@Preview(widthDp = 320, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    onLogout : ()-> Unit,
    onProfileClick : ()-> Unit
) {
    val statusBarController = rememberStatusBarController()

    val loginState by authViewModel.authState.collectAsState()
    var isDarkMode by remember { mutableStateOf(false) }
    var showWelcomeMessage by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLogoutLoader by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val statusColor = MaterialTheme.colorScheme.inversePrimary
    val isSystemInDarkTheme = isSystemInDarkTheme()

    LaunchedEffect(statusColor, isSystemInDarkTheme) {
        statusBarController.updateStatusBar(
            color = statusColor,
            darkIcons = !isSystemInDarkTheme
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings",
                        style = MaterialTheme.typography.titleLarge.responsive()) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.inversePrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) { paddingValues ->

        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("Close")
                    }
                },
                title = { Text("Appearance Settings") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Choose your side")
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Dark Mode")
                            ThemeToggleSwitch(
                                isDark = isDarkMode,
                                onToggle = {
                                    isDarkMode = it
                                    showWelcomeMessage = true
                                    settingsViewModel.toggleDarkMode()
                                }
                            )
                        }
                    }
                }
            )
        }

        if (showLogoutDialog){
            ConfirmActionDialog(
                title = "Logout?",
                message = "Are you sure you want to logout of HaloCare?",
            onConfirm = {
                authViewModel.logout()
                showLogoutDialog = false
                showLogoutLoader = true
                        },
            onDismiss = { showLogoutDialog = false })
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
           if(showLogoutLoader){
               AnimatedLoadingDialog(
                   uiState = loginState,
                   loadingPrompt = "Logging out...",
                   successPrompt = "Successful!",
               ) {
                   if (loginState is AuthUiState.Success){
                       onLogout()
                       authViewModel.resetAuthState()
                       showLogoutDialog = false
                   }
                   if (loginState is AuthUiState.Error){
                       val errorMessage = (loginState as AuthUiState.Error).message
                       Toast.makeText(
                           context,
                           "Error $errorMessage",
                           Toast.LENGTH_SHORT
                       ).show()
                       showLogoutDialog = false
                   }
               }
           }

            if (showWelcomeMessage) {
                val alphaAnim by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600),
                    label = "FadeIn"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .alpha(alphaAnim),
                    contentAlignment = Alignment.Center
                ) {
                    val message = if (isDarkMode) "Welcome to the Dark Side 🌑" else "Welcome to the Light Side ☀️"
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Auto-hide message after delay
                LaunchedEffect(Unit) {
                    delay(2000)
                    showWelcomeMessage = false
                }
            }

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
                    showThemeDialog = true
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Text("Account", style = MaterialTheme.typography.titleMedium)
                SettingItem(
                    title = "Profile Info",
                    subtitle = "View and edit your profile",
                    icon = Icons.Default.Person
                ) {
                    onProfileClick()
                }

                SettingItem(
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    icon = Icons.Default.ExitToApp
                ) {
                    showLogoutDialog = true
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
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp.responsiveWidth()))
        Spacer(modifier = Modifier.width(12.dp.responsiveWidth()))
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium.responsive(),)
            Text(subtitle, style = MaterialTheme.typography.bodySmall.responsive())
        }
    }
}
@Composable
fun ThemeToggleSwitch(
    isDark: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val transition = updateTransition(targetState = isDark, label = "ToggleTransition")

    // Animate the thumb's horizontal offset
    val thumbOffset by transition.animateDp(label = "ThumbOffset") {
        if (it) 28.dp else 0.dp
    }

    // Animate the track color
    val trackColor by transition.animateColor(label = "TrackColor") {
        if (it) Color(0xFF333333) else Color(0xFFE0E0E0)
    }

    // Determine the icon and its tint based on the state
    val icon = if (isDark) R.drawable.baseline_nights_stay_24 else R.drawable.baseline_wb_sunny_24
    val iconTint = if (isDark) Color.White else Color(0xFF1E1E1E)

    // The main container for the switch
    Box(
        modifier = Modifier
            .width(60.dp.responsiveWidth())
            .height(32.dp.responsiveHeight())
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .clickable { onToggle(!isDark) }
            .padding(4.dp)
    ) {
        // The animated thumb, which now contains the icon
        Box(
            modifier = Modifier
                .size(24.dp)
                .offset(x = thumbOffset)
                .background(Color.White, shape = CircleShape)
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "ModeIcon",
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
