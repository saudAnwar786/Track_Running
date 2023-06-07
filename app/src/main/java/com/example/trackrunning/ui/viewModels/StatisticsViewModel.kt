package com.example.trackrunning.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.trackrunning.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: MainRepository
) :ViewModel(){

    val totalTime = repository.getTotalTimeInMillis()
    val totalCalories = repository.getTotalCaloriesBurned()
    val totalDistance = repository.getTotalDistance()
    val totalAvgSpeed = repository.getTotalAvgSpeed()

    val runsSortedByDate = repository.getRunSortedByDate()

}