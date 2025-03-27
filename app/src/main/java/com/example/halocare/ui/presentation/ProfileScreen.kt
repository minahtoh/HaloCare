package com.example.halocare.ui.presentation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.halocare.ui.models.User
import com.example.halocare.viewmodel.AuthUiState
import com.example.halocare.viewmodel.AuthViewModel
import java.io.File
import java.io.IOException


//@Preview(widthDp = 320, heightDp = 720)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onContinue: () -> Unit = {},
    onSkip: () -> Unit = {},
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val loggedUser by authViewModel.haloCareUser.collectAsState()
    val profileState by authViewModel.authState.collectAsState()
    var name by remember { mutableStateOf(loggedUser.name) }
    var nickname by remember { mutableStateOf(loggedUser.nickname ?:"") }
    var phoneNumber by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf(loggedUser.profession ?: "") }
    var imageUri by remember { mutableStateOf(loggedUser.profilePicture) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val savedPath = saveImageToInternalStorage(context, uri!!)
        imageUri = savedPath ?: ""
    }
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val isConnected = connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true


    // Fetch image from backend when online
    LaunchedEffect(Unit) {
        if (isConnected) {
            try {
              //  val backendImageUrl = fetchImageFromBackend(userId) // Replace with actual API call
//                imageUri = Uri.parse(backendImageUrl)
//                dataStore.saveString("profile_image_uri", backendImageUrl) // Save for offline use
            } catch (e: Exception) {
                Log.e("ProfilePicture", "Failed to fetch image, using local fallback", e)
            }
        } else {
            // Load last saved URI from DataStore when offline
//            val savedUri = dataStore.getString("profile_image_uri")
//            if (savedUri.isNotEmpty()) {
//                imageUri = Uri.parse(savedUri)
//            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedLoadingDialog(uiState = profileState) {
                if (profileState is AuthUiState.Success){
                    authViewModel.resetAuthState()
                    authViewModel.refreshUserStatus(loggedUser.uid)
                    onContinue()
                }
                if (profileState is AuthUiState.Error){
                    authViewModel.resetAuthState()
                    val errorMessage = (profileState as AuthUiState.Error).message
                    Toast.makeText(
                        context,
                        "Error $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            Column {
                ProfileTopBar()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(5.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        imageUri?.let {
                            Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(context)
                                            .data(File(it))
                                            .transformations(CircleCropTransformation())
                                            .build()
                                    ),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } ?: Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Add Photo",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25.dp),
                        enabled = isFieldEditable(loggedUser.name)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Nickname") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25.dp),
                        enabled = isFieldEditable(loggedUser.nickname)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = profession,
                        onValueChange = { profession = it },
                        label = { Text("Profession") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25.dp),
                        enabled = isFieldEditable(loggedUser.profession)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = loggedUser.email,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25.dp),
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val updatedUser = User(
                                uid = loggedUser.uid,
                                name = name,
                                nickname = nickname,
                                email = loggedUser.email,
                                profilePicture = imageUri,
                                profession = profession,
                                dateCreated = loggedUser.dateCreated,
                                dateOfBirth = loggedUser.dateOfBirth
                            )
                            authViewModel.updateUser(updatedUser)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text("Save")
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    TextButton(onClick = onSkip) {
                        Text(
                            text = "Do Later",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

fun isFieldEditable(value : String?) : Boolean{
    return value.isNullOrEmpty() || value.isBlank()
}
fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    val fileName = "profile_picture.jpg"
    val file = File(context.filesDir, fileName)

    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file.absolutePath // Return saved file path
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ProfileTopBar(){
    Surface(
        color = MaterialTheme.colorScheme.surfaceTint,
      //  shape = RoundedCornerShape(bottomEnd = 5.dp, bottomStart = 5.dp),
        shadowElevation = 20.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Set Up Your Profile",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onError
            )
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }
}
