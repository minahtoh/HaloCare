package com.example.halocare.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.halocare.database.DailyExerciseSummary
import com.example.halocare.database.ExerciseTrackerDao
import com.example.halocare.database.MoodEntryDao
import com.example.halocare.database.SleepDao
import com.example.halocare.network.NetworkRepository
import com.example.halocare.network.models.WeatherResponse
import com.example.halocare.network.models.WeatherResponseHourly
import com.example.halocare.services.ExerciseTimerService
import com.example.halocare.services.TimerRepository
import com.example.halocare.ui.models.Appointment
import com.example.halocare.ui.models.ExerciseData
import com.example.halocare.ui.models.HaloMoodEntry
import com.example.halocare.ui.models.Professional
import com.example.halocare.ui.models.ProfessionalSpecialty
import com.example.halocare.ui.models.SleepData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

enum class LoadingState{LOADING, SUCCESSFUL, ERROR, IDLE}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val networkRepository: NetworkRepository,
    private val mainRepository: MainRepository,
    private val timerRepository: TimerRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()
    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData = _weatherData.asStateFlow()
    private val _weatherLoadingState = MutableStateFlow(LoadingState.IDLE)
    val weatherLoadingState = _weatherLoadingState.asStateFlow()
    private val _hourlyWeatherData = MutableStateFlow<WeatherResponseHourly?>(null)
    val hourlyWeatherData = _hourlyWeatherData.asStateFlow()
    private val _hourlyWeatherLoadingState = MutableStateFlow(LoadingState.IDLE)
    val hourlyWeatherLoadingState = _hourlyWeatherLoadingState.asStateFlow()
    private val _appointmentBookingLoadingState = MutableStateFlow(LoadingState.IDLE)
    val appointmentBookingState = _appointmentBookingLoadingState.asStateFlow()
    private val _appointmentsList = MutableStateFlow<List<Appointment>?>( null)
    val appointmentsList = _appointmentsList.asStateFlow()
    private val _appointmentListLoadingState = MutableStateFlow(LoadingState.IDLE)
    val appointmentListLoadingState = _appointmentListLoadingState.asStateFlow()
    private val _currentUserId = MutableStateFlow("")
    val currentUserId = _currentUserId.asStateFlow()
    private val _availableSpecialties = MutableStateFlow<List<ProfessionalSpecialty>?>(null)
    val availableSpecialties = _availableSpecialties.asStateFlow()
    private val _dailyMoods = MutableStateFlow<List<HaloMoodEntry>?>(null)
    val dailyMoods = _dailyMoods.asStateFlow()
    private val _todayAdvice = MutableStateFlow<String?>(null)
    val todayAdvice = _todayAdvice.asStateFlow()
    private val _exerciseDataList = MutableStateFlow<List<ExerciseData>?>(null)
    val exerciseDataList = _exerciseDataList.asStateFlow()
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning
    val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(receivedContext: Context?, intent: Intent?) {

            if (intent?.action == ExerciseTimerService.BROADCAST_ACTION_STOPPED) {
                val elapsedTimed = intent.getIntExtra(ExerciseTimerService.EXTRA_ELAPSED_TIME, 0)

                _isRunning.value = false
                Log.d("RunningTimer", "onReceive: isrunnning status $isRunning")
            }
        }
    }

    val currentTime: StateFlow<Int> = timerRepository.currentTimeSeconds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = timerRepository.currentTimeSeconds.value // Read initial directly
        )

    val exerciseName: StateFlow<String> = timerRepository.currentExerciseName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "" // Start empty, flow will update from repo/DataStore
        )

    val allSleepData = mainRepository.getAllSleepData()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    init {
        registerReceiver()
        getTodayWeather("Lagos")
        updateCurrentUser()
        getAvailableSpecialties()
    }
    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(getApplication(context)).unregisterReceiver(timerReceiver)
    }

    private fun updateCurrentUser(){
        val currentUser = authRepository.getCurrentUser()
        _currentUserId.value = currentUser?.uid ?: ""
    }
    private fun registerReceiver() {
        val filter = IntentFilter(ExerciseTimerService.BROADCAST_ACTION_STOPPED)

        LocalBroadcastManager.getInstance(getApplication(context)).registerReceiver(timerReceiver, filter)
    }

    private fun getAvailableSpecialties(){
        viewModelScope.launch {
            val specialtyData = mainRepository.getListOfSpecialties()
            specialtyData.onSuccess {
                _availableSpecialties.value = it
                Log.d("AVAILABLESPECIALTY", "getAvailableSpecialties: $it")
            }.onFailure {data->
                val errorMessage = data.message
                errorMessage?.let {
                    _toastMessage.tryEmit(it)
                }
                Log.d("AVAILABLESPECIALTY", "getAvailableSpecialties Error: $errorMessage")
            }
        }
    }

    fun getTodayWeather(location: String){
        viewModelScope.launch {
            _weatherLoadingState.value = LoadingState.LOADING
            val weatherCall = networkRepository.getTodayWeather(location)
            if (weatherCall.isSuccess){
                _weatherData.value = weatherCall.getOrNull()
                _weatherLoadingState.value = LoadingState.SUCCESSFUL
            }
            if (weatherCall.isFailure){
                val errorData = weatherCall.exceptionOrNull()
                Log.d("WEATHER DATA", "getTodayWeather: ${errorData?.message}")
                _weatherLoadingState.value = LoadingState.ERROR
            }
        }
    }

    fun getHourlyWeather(location: String){
        viewModelScope.launch {
            _hourlyWeatherLoadingState.value = LoadingState.LOADING
            delay(1500)
            val hourlyWeatherCall = networkRepository.getHourlyWeather(location)
            if (hourlyWeatherCall.isSuccess){
                _hourlyWeatherData.value = hourlyWeatherCall.getOrNull()
                _hourlyWeatherLoadingState.value = LoadingState.SUCCESSFUL
            }
            if(hourlyWeatherCall.isFailure){
                val errorData = hourlyWeatherCall.exceptionOrNull()
                Log.d("WEATHER DATA", "getTodayWeather: ${errorData?.message}")
                _hourlyWeatherLoadingState.value = LoadingState.ERROR
            }
        }
    }

    fun bookUserAppointment(userId: String, appointment: Appointment){
        viewModelScope.launch {
            _appointmentBookingLoadingState.value = LoadingState.LOADING
            delay(1500)
            val result = mainRepository.bookUserAppointment(userId, appointment)
            result.onSuccess {
                _appointmentBookingLoadingState.value = LoadingState.SUCCESSFUL
            }.onFailure {
                _appointmentBookingLoadingState.value = LoadingState.ERROR
            }
        }
    }

    fun getUserAppointments(userId: String){
        viewModelScope.launch {
            _appointmentListLoadingState.value = LoadingState.LOADING
            delay(1500)
            val result = mainRepository.getUserAppointment(userId)
            result.onSuccess {
                _appointmentsList.value = it
                _appointmentListLoadingState.value = LoadingState.SUCCESSFUL
            }.onFailure {
                _appointmentListLoadingState.value = LoadingState.ERROR
            }
        }
    }

    fun resetBookingState(){
        viewModelScope.launch {
            _appointmentBookingLoadingState.value = LoadingState.IDLE
        }
    }

    fun getTodaysMood(startTime: Long, endTime: Long){
        viewModelScope.launch {
       mainRepository
             .getDailyMoodEntry(startTime, endTime)
                    .catch {
                            exception ->
                        // Handle errors from the flow
                        Log.e("VIEWMODEL", "Error collecting mood flow", exception)
                        _dailyMoods.value = emptyList()
                 }.collect {
               _dailyMoods.value = it
           }
        }
    }

    fun logMoodEntry(moodEntry: HaloMoodEntry){
        viewModelScope.launch {
            mainRepository.logMoodEntry(moodEntry)
        }
    }

    fun getTodayAdvice(){
        viewModelScope.launch {
           val advice = networkRepository.getRandomAdvice()
            if (advice.isSuccess){
                _todayAdvice.value = advice.getOrNull()?.slip?.advice
            }
            if (advice.isFailure){
                _toastMessage.tryEmit(advice.exceptionOrNull()?.message ?: "Network error!")
            }
        }
    }

    fun getExerciseDataList(){
        viewModelScope.launch {
            mainRepository.getExerciseDataList().catch {  }.collect{
                _exerciseDataList.value = it.reversed()
                Log.d("CHARTSAVER", "saveExerciseData: saved $it")

            }
        }
    }
    fun saveExerciseData(exerciseData: ExerciseData){
        viewModelScope.launch {
            mainRepository.saveExerciseData(exerciseData)
            Log.d("CHARTSAVER", "saveExerciseData: saved $exerciseData")
        }
    }
    fun onExerciseNameChange(newName: String) {
        viewModelScope.launch {
            timerRepository.saveExerciseName(newName)
        }
    }

    fun clearTimerState() {
        viewModelScope.launch {
            timerRepository.clearPersistedState()
        }
    }

    fun logSleepData(sleepData: SleepData){
        viewModelScope.launch {
            mainRepository.insertSleep(sleepData)
        }
    }
}
@Singleton
class MainRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val moodEntryDao: MoodEntryDao,
    private val exerciseTrackerDao: ExerciseTrackerDao,
    private val sleepDao: SleepDao
){
    suspend fun bookUserAppointment(userId: String, appointment: Appointment): Result<Boolean>{
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("appointments")
                .add(appointment).await()
            Result.success(true)
        }catch (e:Exception){
            Result.failure(e)
        }
    }

    suspend fun getUserAppointment(userId: String): Result<List<Appointment>>{
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("appointments")
                .orderBy("bookedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val appointments = snapshot.documents.mapNotNull { it.toObject(Appointment::class.java) }

            Result.success(appointments)
        } catch (e:Exception){
            Result.failure(e)
        }
    }

    suspend fun getListOfSpecialties() : Result<List<ProfessionalSpecialty>>{
        val specialtiesCollection = firestore.collection("specialties")
        val specialtiesList = mutableListOf<ProfessionalSpecialty>()

        return try{
            val specialtiesSnapshot = specialtiesCollection.get().await()

            for (specialtyDoc in specialtiesSnapshot.documents) {
                val specialtyName = specialtyDoc.getString("specialty") ?: continue
                val professionalsSnapshot = specialtyDoc.reference
                    .collection("professionals")
                    .get()
                    .await()

                val professionals = professionalsSnapshot.mapNotNull {
                    val prof = it.toObject(Professional::class.java)
                    prof.copy(id = it.id)
                }
                specialtiesList.add(
                    ProfessionalSpecialty(
                        specialtyName = specialtyName,
                        professionals = professionals
                    )
                )
            }

            Result.success(specialtiesList)

        }catch (e:Exception){
            Result.failure(e)
        }
    }

    fun getDailyMoodEntry(startTime:Long, endTime:Long)
    : Flow<List<HaloMoodEntry>> {
       return moodEntryDao.getEntriesForDay(startTime, endTime)
    }
    suspend fun logMoodEntry(moodEntry: HaloMoodEntry){
        withContext(Dispatchers.IO){
            moodEntryDao.insertMoodEntry(moodEntry)
        }
    }
    suspend fun saveExerciseData(exerciseData: ExerciseData){
        withContext(Dispatchers.IO){
            exerciseTrackerDao.insertExercise(exerciseData)
        }
    }

    fun getExerciseDataList() : Flow<List<ExerciseData>>{
        return exerciseTrackerDao.getAllExercises()
    }

    fun getExercisesSummary() : Flow<List<DailyExerciseSummary>>{
        return exerciseTrackerDao.getLast7DailyExercises()
    }

    suspend fun insertSleep(sleepData: SleepData) = sleepDao.insertSleepData(sleepData)
    fun getAllSleepData(): Flow<List<SleepData>> = sleepDao.getAllSleepData()

}