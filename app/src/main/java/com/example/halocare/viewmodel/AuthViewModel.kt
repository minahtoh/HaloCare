package com.example.halocare.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halocare.database.UserDao
import com.example.halocare.ui.models.User
import com.example.halocare.ui.presentation.UiState
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState
    private var _haloCareUserState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val haloCareUserState = _haloCareUserState.asStateFlow()
    private var _haloCareUser = MutableStateFlow(User())
    val haloCareUser = _haloCareUser.asStateFlow()

    fun registerUser(
        name: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                val result = authRepository.registerWithEmail(email, password)
                val firebaseUser = result?.user ?: throw Exception("User not created")
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = name
                )

                val firestoreResult = authRepository.saveUserToFirestore(user)

                if (firestoreResult.isSuccess) {
                    _authState.value = AuthUiState.Success(firebaseUser)
                } else {
                    throw firestoreResult.exceptionOrNull() ?: Exception("Unknown Firestore error")
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "An error occurred. Check Internet Connection")
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                val result = authRepository.loginWithEmail(email, password)
                result?.user?.let { firebaseUser ->
                    val user = authRepository.getUserFromFireStore(firebaseUser.uid)
                    user?.let {
                        authRepository.saveUserLocally(it)
                    }
                    fetchUser(firebaseUser.uid)
                    _authState.value = AuthUiState.Success(firebaseUser)
                } ?: run {
                    _authState.value = AuthUiState.Error("Login failed. Check your credentials.")
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "An error occurred.")
            }
        }
    }

    fun loginUserWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                val result = authRepository.loginWithGoogle(idToken)
                result?.user?.let {
                    _authState.value = AuthUiState.Success(it)
                } ?: run {
                    _authState.value = AuthUiState.Error("Google Sign-In failed.")
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "An error occurred.")
            }
        }
    }

    private fun fetchUser(userId: String) {
        viewModelScope.launch {
            val localUser = authRepository.getUserLocally(userId)
            _haloCareUser.value = localUser ?: kotlin.run {
                val remoteUser = authRepository.getUserFromFireStore(userId)
                remoteUser?.let { authRepository.saveUserLocally(it) }
                remoteUser ?: User()
            }
        }
    }

    fun updateUser(user: User){
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val result = authRepository.updateUserInFireStore(user)
            if (result.isSuccess){
                _authState.value = AuthUiState.Success(null)
            } else{
                _authState.value = AuthUiState.Error("Error updating profile, please try again!")
            }
        }
    }

    fun refreshUserStatus(userId: String){
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            val refreshedUser = currentUser?.let { authRepository.getUserFromFireStore(it.uid) }
            refreshedUser?.let { authRepository.saveUserLocally(it) }
            _haloCareUser.value = refreshedUser ?: authRepository.getUserLocally(userId)!!
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthUiState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthUiState.Idle
    }

    fun resetLoginState(){
        _haloCareUserState.value = UiState.Idle
    }
}

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) {
    suspend fun registerWithEmail(email: String, password: String): AuthResult? {
        return  auth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun loginWithEmail(email: String, password: String): AuthResult? {
        return  auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun loginWithGoogle(idToken: String): AuthResult? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return  auth.signInWithCredential(credential).await()
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser

    suspend fun saveUserToFirestore(user: User): Result<Boolean> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserFromFireStore(userId: String): User? {
        return try {
            val userSnapshot = firestore.collection("users").document(userId).get().await()
            userSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching user: ${e.message}", e)
            null
        }
    }

    suspend fun updateUserInFireStore(user: User) : Result<Boolean>{
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            saveUserLocally(user) // Update local cache
            Result.success(true)
        } catch (e:Exception){
            Log.d("UPDATEUSER", "updateUserInFireStore: ${e.message}")
            Result.failure(e)
        }
    }
    suspend fun saveUserLocally(user: User) {
        withContext(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }

    suspend fun getUserLocally(userId: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUser(userId)
        }
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: FirebaseUser?) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
