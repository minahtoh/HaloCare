package com.example.halocare.module

import android.content.Context
import androidx.room.Room
import com.example.halocare.database.HaloCareDatabase
import com.example.halocare.database.UserDao
import com.example.halocare.viewmodel.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()


    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        userDao: UserDao
    ): AuthRepository {
        return AuthRepository(auth, firestore, userDao)
    }
}