package com.example.trackrunning.di

import android.content.Context
import androidx.room.Room
import com.example.trackrunning.db.RunDao
import com.example.trackrunning.db.RunDatabase
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
}