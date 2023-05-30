package com.example.trackrunning.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert


@Dao
interface RunDao {

    @Upsert
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM run ORDER BY timeStamp DESC")
    fun getRunSortedByDate():LiveData<List<Run>>

    @Query("SELECT * FROM run ORDER BY avgSpeedInKmPerHour DESC")
    fun getRunSortedAvgSpeed():LiveData<List<Run>>

    @Query("SELECT * FROM run ORDER BY calorieBurned DESC")
    fun getRunSortedByCalorieBurned():LiveData<List<Run>>

    @Query("SELECT * FROM run ORDER BY distanceInMeter DESC")
    fun getRunSortedByDistance():LiveData<List<Run>>

    @Query("SELECT * FROM run ORDER BY timeInMillies DESC")
    fun getRunSortedByTimeInMillis():LiveData<List<Run>>

    @Query("SELECT SUM(timeInMillies) FROM run")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(calorieBurned) FROM run")
    fun getTotalCaloriesBurned(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeter) FROM run")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT AVG(avgSpeedInKmPerHour) FROM run")
    fun getTotalAvgSpeed(): LiveData<Float>


}