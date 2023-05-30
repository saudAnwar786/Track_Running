package com.example.trackrunning.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.trackrunning.repository.MainRepository
import javax.inject.Inject

class TrackingViewModel @Inject constructor(
    private val repository: MainRepository
) :ViewModel(){


}