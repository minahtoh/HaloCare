package com.example.halocare.ui.presentation


import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.halocare.ui.models.User
import com.example.halocare.viewmodel.AuthUiState
import com.example.halocare.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay


//@Preview(widthDp = 320, heightDp = 720)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onSuccessfulLogin : ()-> Unit = {},
    onSignupClick : ()-> Unit = {},
    viewModel: AuthViewModel
){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.surface
    LaunchedEffect(key1 = true){
        statusBarController.updateStatusBar(
            color = statusBarColor,
            darkIcons = true
        )
    }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedLoadingDialog(uiState = loginState) {
                if (loginState is AuthUiState.Success){
                    viewModel.resetAuthState()
                   // val loggedUser = (loginState as AuthUiState.Success<User>).data
                    onSuccessfulLogin()
                }
                if (loginState is AuthUiState.Error){
                    viewModel.resetAuthState()
                    val errorMessage = (loginState as AuthUiState.Error).message
                    Toast.makeText(
                        context,
                        "Error $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            Column {
                IntroCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 0.dp
                )
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Spacer(modifier = Modifier.height(30.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(15.dp),
                        shadowElevation = 5.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .height(360.dp)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Welcome Back!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(25.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(25.dp),
                                visualTransformation = PasswordVisualTransformation()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .height(40.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = false, onCheckedChange ={})
                                Text(text = "Remember me")
                            }
                            Button(
                                onClick = {
                                    viewModel.loginUser(email, password)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(25.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                elevation = ButtonDefaults.buttonElevation(2.dp)

                            ) {
                                Text(
                                    text = "Login",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 24.dp, end = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        TextButton(onClick = { /* Navigate to Forgot Password */ }) {
                            Text("Forgot Password?", color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        GoogleSignInSectionWithCustomIcon(onGoogleSignInClick = { /*TODO*/ })

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Don't have an account?")
                            TextButton(
                                onClick = {
                                    onSignupClick()
                                }
                            ) {
                                Text(
                                    "Sign Up",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HaloCareLoginDialog(
    uiState: UiState<FirebaseUser>,
    onDismiss: () -> Unit
) {
    if (uiState is UiState.Idle) return  // Hide when idle

    Dialog(onDismissRequest = onDismiss) {
        val transition = updateTransition(targetState = uiState, label = "loadingTransition")

        val alpha by transition.animateFloat(
            transitionSpec = { tween(durationMillis = 300) },
            label = "alpha"
        ) { state ->
            if (state is UiState.Loading) 1f else 1f
        }

        val offsetY by transition.animateDp(
            transitionSpec = { tween(durationMillis = 300) },
            label = "offsetY"
        ) { state ->
            if (state is UiState.Loading) 0.dp else (-10).dp
        }

        val offsetYPx = with(LocalDensity.current){offsetY.toPx()}

        val scale by transition.animateFloat(
            transitionSpec = { tween(durationMillis = 300, easing = FastOutSlowInEasing) },
            label = "scale"
        ) { state ->
            when (state) {
                is UiState.Loading -> 1f
                is UiState.Success, is UiState.Error -> 1.2f
                else -> 1f
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(24.dp)
                .graphicsLayer(
                    alpha = alpha,
                    translationY = offsetYPx,
                    scaleX = scale,
                    scaleY = scale
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                    is UiState.Success -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = Color.Green,
                            modifier = Modifier
                                .size(48.dp)
                                .scale(scale)
                        )
                    }
                    is UiState.Error -> {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(48.dp)
                                .scale(scale)
                        )
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (uiState) {
                        is UiState.Loading -> "Logging in..."
                        is UiState.Success -> "Login Success!"
                        is UiState.Error -> uiState.message
                        else -> ""
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
            }
        }
    }

    // Auto-dismiss after success/error
    LaunchedEffect(uiState) {
        if (uiState is UiState.Success || uiState is UiState.Error) {
            delay(1500)
            onDismiss()
        }
    }
}
@Composable
fun GoogleSignInSectionWithCustomIcon(
    onGoogleSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Divider with "OR" text
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Google Sign In Button with custom icon
        OutlinedButton(
            onClick = onGoogleSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Custom Google "G" icon
                Canvas(modifier = Modifier.size(24.dp)) {
                    val radius = size.width / 2
                    val center = Offset(radius, radius)

                    // Draw the "G" shape with Google colors
                    drawCircle(
                        color = Color(0xFF4285F4),
                        radius = radius * 0.9f,
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = radius * 0.7f,
                        center = center
                    )

                    // Simple "G" representation
                    drawPath(
                        path = Path().apply {
                            moveTo(radius * 0.5f, radius * 0.7f)
                            lineTo(radius * 1.2f, radius * 0.7f)
                            lineTo(radius * 1.2f, radius)
                            lineTo(radius * 1.5f, radius)
                            lineTo(radius * 1.5f, radius * 1.3f)
                            lineTo(radius * 0.5f, radius * 1.3f)
                            close()
                        },
                        color = Color(0xFF4285F4)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}



sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}