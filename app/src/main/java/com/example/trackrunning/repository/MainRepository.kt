package com.example.trackrunning.repository

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.trackrunning.db.Run
import com.example.trackrunning.db.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val dao: RunDao
) {

    suspend fun insertRun(run: Run){
        dao.insertRun(run)
    }


    suspend fun deleteRun(run: Run){
        dao.deleteRun(run)
    }


    fun getRunSortedByDate(): LiveData<List<Run>>{
        return dao.getRunSortedByDate()
    }


    fun getRunSortedAvgSpeed(): LiveData<List<Run>>{
        return dao.getRunSortedAvgSpeed()
    }


    fun getRunSortedByCalorieBurned(): LiveData<List<Run>>{
        return dao.getRunSortedByCalorieBurned()
    }


    fun getRunSortedByDistance(): LiveData<List<Run>>{
        return dao.getRunSortedByDistance()
    }
    fun getRunSortedByTimeInMillis():LiveData<List<Run>>{
        return dao.getRunSortedByTimeInMillis()
    }



    fun getTotalTimeInMillis(): LiveData<Long>{
        return dao.getTotalTimeInMillis()
    }


    fun getTotalCaloriesBurned(): LiveData<Int>{
        return dao.getTotalCaloriesBurned()
    }


    fun getTotalDistance(): LiveData<Int>{
        return dao.getTotalDistance()
    }


    fun getTotalAvgSpeed(): LiveData<Float>{
        return dao.getTotalAvgSpeed()
    }
}