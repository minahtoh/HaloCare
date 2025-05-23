package com.example.halocare.module

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.halocare.database.ExerciseTrackerDao
import com.example.halocare.database.HaloCareDatabase
import com.example.halocare.database.JournalDao
import com.example.halocare.database.MedicationsDao
import com.example.halocare.database.MoodEntryDao
import com.example.halocare.database.SleepDao
import com.example.halocare.database.UserDao
import com.example.halocare.network.NetworkConstants
import com.example.halocare.services.TimerRepository
import com.example.halocare.services.TimerRepositoryImpl
import com.example.halocare.viewmodel.AuthRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val TIMER_PREFERENCES = "timer_prefs"

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        // Create the DataStore instance
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(TIMER_PREFERENCES)
        }
    }
    @Provides
    @Singleton
    fun provideTimerRepository(
        impl: TimerRepositoryImpl
    ): TimerRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HaloCareDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            HaloCareDatabase::class.java,
            "halocare_db"
        ).fallbackToDestructiveMigration().build()
    }
    @Provides
    fun provideUserDao(database: HaloCareDatabase): UserDao {
        return database.userDao()
    }
    @Provides
    fun provideMoodEntryDao(database: HaloCareDatabase): MoodEntryDao{
        return database.moodEntryDao()
    }
    @Provides
    fun provideExerciseTrackerDao(database: HaloCareDatabase): ExerciseTrackerDao{
        return database.exerciseDao()
    }
    @Provides
    fun provideSleepTrackerDao(database: HaloCareDatabase): SleepDao{
        return database.sleepDao()
    }
    @Provides
    fun provideJournalDao(database: HaloCareDatabase): JournalDao{
        return database.journalDao()
    }
    @Provides
    fun provideMedicationsDao(database: HaloCareDatabase): MedicationsDao{
        return database.medicationsDao()
    }


    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance("gs://your-target-bucket.appspot.com") // 👈 Use the correct bucket name
    }



    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        userDao: UserDao
    ): AuthRepository {
        return AuthRepository(auth, firestore, userDao)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
             level = HttpLoggingInterceptor.Level.BODY
        }).build()
    }

    @Provides
    @Singleton
    @Named("weatherApi")
    fun provideWeatherRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkConstants.WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    @Named("adviceApi")
    fun provideAdviceRetrofit(client: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .baseUrl(NetworkConstants.ADVICE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}