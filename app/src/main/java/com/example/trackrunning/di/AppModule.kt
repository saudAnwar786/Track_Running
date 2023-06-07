package com.example.trackrunning.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.trackrunning.db.RunDao
import com.example.trackrunning.db.RunDatabase
import com.example.trackrunning.other.Constants
import com.example.trackrunning.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.trackrunning.other.Constants.KEY_NAME
import com.example.trackrunning.other.Constants.KEY_WEIGHT
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
    fun provideDatabase(@ApplicationContext appContext: Context): RunDatabase {
        return Room.databaseBuilder(
            appContext,
            RunDatabase::class.java,
            "my_database"
        ).build()
    }
    @Provides
    @Singleton
    fun provideDao(runDatabase: RunDatabase)  :RunDao {
        return runDatabase.getRunDao()
    }

    @Provides
    @Singleton
    fun provideSharePref(@ApplicationContext context: Context) :SharedPreferences {
       return  context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
    }
    @Provides
    @Singleton
    fun providename(sharedPreferences: SharedPreferences) = sharedPreferences
        .getString(KEY_NAME,"")?:""

    @Provides
    @Singleton
    fun provideWeight(sharedPreferences: SharedPreferences) = sharedPreferences
        .getFloat(KEY_WEIGHT,80f)

    @Provides
    @Singleton
    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences) = sharedPreferences
        .getBoolean(KEY_FIRST_TIME_TOGGLE,true)
}