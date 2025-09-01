package com.example.halocare.ui.presentation

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.halocare.R
import com.example.halocare.viewmodel.AuthUiState
import com.example.halocare.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import responsiveSp


//@Preview(widthDp = 320, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onSignUpSuccess : () -> Unit = {},
    onLoginClick : () -> Unit = {},
    authViewModel: AuthViewModel,
    isDarkMode : Boolean
){
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val statusBarController = rememberStatusBarController()
    val statusBarColor = MaterialTheme.colorScheme.surface

    LaunchedEffect(key1 = true){
        statusBarController.updateStatusBar(
            color = statusBarColor,
            darkIcons = isDarkMode
        )
    }


    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedLoadingDialog(
                uiState = uiState,
                loadingPrompt = "Registering user",
                successPrompt = "User successfully created!"
            ) {
                if (uiState is AuthUiState.Success){
                    authViewModel.resetAuthState()
                    onSignUpSuccess()
                }
                if (uiState is AuthUiState.Error){
                    authViewModel.resetAuthState()
                    Toast.makeText(
                        context,
                        "Error ${(uiState as AuthUiState.Error).message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                IntroCard(
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(25.dp.responsiveHeight()))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Create your Account",
                        style = MaterialTheme.typography.headlineSmall.responsive(),
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(16.dp.responsiveHeight()))

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(30.dp.responsiveHeight())
                    )

                    Spacer(modifier = Modifier.height(12.dp.responsiveHeight()))

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(30.dp.responsiveHeight())
                    )

                    Spacer(modifier = Modifier.height(12.dp.responsiveHeight()))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(30.dp.responsiveHeight())
                    )

                    Spacer(modifier = Modifier.height(12.dp.responsiveHeight()))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(30.dp.responsiveHeight())
                    )

                    Spacer(modifier = Modifier.height(60.dp.responsiveHeight()))

                    Button(
                        onClick = {
                            authViewModel.registerUser(
                                name = "$firstName $lastName",
                                email = email,
                                password = password
                            )
                        },
                        modifier = Modifier
                            .height(55.dp.responsiveHeight())
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp),
                        elevation = ButtonDefaults.buttonElevation(3.dp)
                    ) {
                        Text("Sign Up")
                    }

                    Spacer(modifier = Modifier.height(12.dp.responsiveHeight()))

                    TextButton(
                        onClick = {
                            onLoginClick()
                        }
                    ) {
                        Text("Already have an account? Log in")
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 320)
@Composable
fun IntroCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 15.dp
){
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        shadowElevation = elevation
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(25.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.mobile_app_logo__for_halocare__no_text_),
                contentDescription = "app_icon",
                modifier = Modifier.size(150.dp.responsiveHeight())
            )
            Spacer(modifier = Modifier.height(10.dp.responsiveHeight()))
            Text(
                text = "Track Your Wellness with us!",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AnimatedLoadingDialog(
    uiState: AuthUiState,  // Tracks loading, success, or error
    loadingPrompt: String,
    successPrompt: String,
    onDismiss: () -> Unit,
) {
    if (uiState is AuthUiState.Idle) return // Don't show if idle

    Dialog(onDismissRequest = {
        if(uiState != AuthUiState.Loading) onDismiss()
    }) {

        val scale by animateFloatAsState(
            targetValue = if (uiState is AuthUiState.Loading) 1f else 1.2f,
            animationSpec = tween(durationMillis = 300),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .width(280.dp.responsiveWidth())
                .height(200.dp.responsiveHeight())
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (uiState) {
                    is AuthUiState.Loading -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                    is AuthUiState.Success -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = Color.Green,
                            modifier = Modifier
                                .size(48.dp)
                                .scale(scale)
                        )
                    }
                    is AuthUiState.Error -> {
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
                        is AuthUiState.Loading -> loadingPrompt
                        is AuthUiState.Success -> successPrompt
                        is AuthUiState.Error -> uiState.message
                        else -> ""
                    },
                    fontSize = 16.sp.responsiveSp(),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
            }
        }
    }

    // Auto-dismiss on success/error after 1.5s
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success || uiState is AuthUiState.Error) {
            delay(1500)
            onDismiss()
        }
    }
}
