package com.example.trackrunning.services
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.trackrunning.R
import com.example.trackrunning.other.Constants
import com.example.trackrunning.other.TrackingUtility
import com.example.trackrunning.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.timer


typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService: LifecycleService(){

     private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
     private var timeInSeconds = MutableLiveData<Long>()
   companion object{
       const val TAG = "TrackingService"
       var isTracking  = MutableLiveData<Boolean>()
       var pathPoints = MutableLiveData<Polylines>()
       var timeInMillies = MutableLiveData<Long>()

   }
    var isFirstRun = true

    override fun onCreate() {
        super.onCreate()
        addInitialValue()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocation(it)
        })
    }
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        isTimerEnabled = true
        timeStarted = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
        while(isTracking.value!!) {
            lapTime = System.currentTimeMillis() - timeStarted
            timeInMillies.postValue(timeRun + lapTime)
            if (timeInMillies.value!! >= lastSecondTimestamp + 1) {
                timeInSeconds.postValue(lastSecondTimestamp + 1)

            }
            delay(Constants.TIMER_UPDATE_INTERVAL)
            timeRun += lapTime
        }
        }


    }
    private fun pauseTracking(){
        isTracking.postValue(false)

    }
    private fun addInitialValue(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeInSeconds.postValue(0L)
        timeInMillies.postValue(0L)
    }
    private fun addEmptyPolyline() =
        pathPoints.value?.apply {
            add(mutableListOf())
            pathPoints.postValue(this)
        }?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoints(location:Location){
        pathPoints.value?.apply {
            val loc = LatLng(location.latitude,location.longitude)
            last().add(loc)
            pathPoints.postValue(this)

        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocation(isTracking:Boolean){
        if (isTracking){
            if(TrackingUtility.hasLocationPermission(this)) {
                val req = com.google.android.gms.location.LocationRequest.create().apply {
                     interval = Constants.LOCATION_UPDATE_INTERVAL
                    fastestInterval = Constants.FASTEST_LOCATION_INTERVAL
                    priority = LocationRequest.QUALITY_HIGH_ACCURACY
                }

                fusedLocationProviderClient.requestLocationUpdates(req,locationCallback, Looper.getMainLooper())
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        }

    }
    val locationCallback = object :LocationCallback(){
        override fun onLocationResult(res: LocationResult) {
            super.onLocationResult(res)

            if(isTracking.value!!){
                res.locations?.let { locations->
                    for(location in locations){
                        addPathPoints(location)
                        Log.d(TAG,"Latitude: ${location.latitude},Longitude: ${location.longitude}")
                    }
                }

            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                Constants.ACTION_START_OR_RESUME_SERVICE->{
                    if(isFirstRun){
                        startForegroundService()
                        isFirstRun= false
                    }else{
                        startTimer()
                    }
                }
                Constants.ACTION_PAUSE_SERVICE->{
                    pauseTracking()
                }
                Constants.ACTION_STOP_SERVICE->{
                    isTracking.postValue(false)
                    Log.d(TAG,"Service stopped")
                }
                else -> {

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private fun startForegroundService(){
        startTimer()
        isTracking.postValue(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
        as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        val notificationBuilder = NotificationCompat.Builder(this,
        Constants.NOTIFICATION_CHANNEL_ID
            ).setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle("Tracking")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(Constants.NOTIFICATION_ID,notificationBuilder.build())
        Log.d(TAG,"Foregroung has satrted")

    }
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this,MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_MUTABLE,

    )
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID,Constants.NOTIFICATION_CHANNEL_NAME,
                      IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }

}