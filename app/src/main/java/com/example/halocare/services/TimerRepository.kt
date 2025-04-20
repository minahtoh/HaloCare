package com.example.halocare.services

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.function.BooleanSupplier
import javax.inject.Inject
import javax.inject.Singleton


interface TimerRepository {
    val currentTimeSeconds: StateFlow<Int>
    val currentExerciseName: StateFlow<String>
    val isTimerRunning : StateFlow<Boolean?>
    suspend fun loadInitialState()
    suspend fun saveExerciseName(name: String)
    suspend fun updateTimeAndPersist(time: Int)
    suspend fun clearPersistedState()
    suspend fun isTimerRunning(status: Boolean?)
}

@Singleton
class TimerRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TimerRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private object PreferencesKeys {
        val EXERCISE_NAME_KEY = stringPreferencesKey("current_exercise_name")
        val TIMER_SECONDS_KEY = intPreferencesKey("current_timer_seconds")
    }

    // --- In-Memory State Flows ---
    private val _currentTimeSeconds = MutableStateFlow(0)
    override val currentTimeSeconds: StateFlow<Int> = _currentTimeSeconds.asStateFlow()

    private val _currentExerciseName = MutableStateFlow("")
    override val currentExerciseName: StateFlow<String> = _currentExerciseName.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    override val isTimerRunning: StateFlow<Boolean?> = _isTimerRunning.asStateFlow()

    init {
        repositoryScope.launch { loadInitialState() }
    }

    override suspend fun loadInitialState() {
        dataStore.data.catch{}.firstOrNull()?.let { prefs ->
            val loadedName = prefs[PreferencesKeys.EXERCISE_NAME_KEY] ?: ""
            val loadedTime = prefs[PreferencesKeys.TIMER_SECONDS_KEY] ?: 0
            _currentExerciseName.value = loadedName
            _currentTimeSeconds.value = loadedTime
    }
    }

    override suspend fun saveExerciseName(name: String) {
        _currentExerciseName.value = name
        dataStore.edit { prefs -> prefs[PreferencesKeys.EXERCISE_NAME_KEY] = name }
    }

    override suspend fun updateTimeAndPersist(time: Int) {
        _currentTimeSeconds.value = time
        if (time % 5 == 0 || time <= 1) {
            dataStore.edit { prefs -> prefs[PreferencesKeys.TIMER_SECONDS_KEY] = time }
        }
    }

    override suspend fun clearPersistedState() {
        dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.EXERCISE_NAME_KEY)
            prefs.remove(PreferencesKeys.TIMER_SECONDS_KEY)
        }
        _currentExerciseName.value = ""
        _currentTimeSeconds.value = 0
    }

    override suspend fun isTimerRunning(status:Boolean?) {
        _isTimerRunning.value = status ?: false
    }
}