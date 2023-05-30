package com.example.trackrunning.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Run(
    @PrimaryKey(autoGenerate = true)
    val id:Int? = null,
    var image : Bitmap? = null,
    var timeStamp:Long = 0L,
    var avgSpeedInKmPerHour : Float = 0f,
    var calorieBurned :Int =0,
    var timeInMillies:Long = 0L,
    var distanceInMeter:Int = 0
)
