package com.example.halocare.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.halocare.ui.models.ExerciseData
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseTrackerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseData)

    @Query("""
    SELECT exerciseDate, SUM(timeElapsed) as totalTime 
    FROM exercises_table 
    GROUP BY exerciseDate 
    ORDER BY exerciseDate DESC 
    LIMIT 7
""")
    fun getLast7DailyExercises(): Flow<List<DailyExerciseSummary>>

    @Query("SELECT * FROM exercises_table ORDER BY exerciseId DESC")
    fun getAllExercises(): Flow<List<ExerciseData>>
}

data class DailyExerciseSummary(
    val exerciseDate: String,
    val totalTime: Float
)