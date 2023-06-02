package com.example.trackrunning.services
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
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
import com.example.trackrunning.other.Constants.ACTION_PAUSE_SERVICE
import com.example.trackrunning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.trackrunning.other.Constants.NOTIFICATION_ID
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
import javax.inject.Inject
import kotlin.concurrent.timer


typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService: LifecycleService(){

     @Inject
     lateinit var fusedLocationProviderClient : FusedLocationProviderClient

     @Inject
     lateinit var baseNotificationBuilder: NotificationCompat.Builder

     lateinit var currNotificationBuilder:NotificationCompat.Builder
     private var timeInSeconds = MutableLiveData<Long>()

   companion object{
       const val TAG = "TrackingService"
       var isTracking  = MutableLiveData<Boolean>()
       var pathPoints = MutableLiveData<Polylines>()
       var timeInMillies = MutableLiveData<Long>()

   }
    var isFirstRun = true
    var isKilledService = false

    override fun onCreate() {
        super.onCreate()
        currNotificationBuilder = baseNotificationBuilder
        addInitialValue()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocation(it)
            updateNotification(it)
        })
    }
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun killService(){
        isKilledService =true
        isFirstRun = true
        pauseTracking()
        addInitialValue()
        stopForeground(true)
        stopSelf()
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
                   killService()
                }
                else -> {

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateNotification(isTracking: Boolean){
        val notificationActionText = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        currNotificationBuilder = baseNotificationBuilder
            .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
        notificationManager.notify(NOTIFICATION_ID, currNotificationBuilder.build())
    }


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
                lastSecondTimestamp+=1000L

            }
            delay(Constants.TIMER_UPDATE_INTERVAL)

        }
            timeRun += lapTime
        }


    }
    private fun pauseTracking(){
        isTracking.postValue(false)
         isTimerEnabled = false
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


    private fun startForegroundService(){
        startTimer()
        isTracking.postValue(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
        as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(Constants.NOTIFICATION_ID,baseNotificationBuilder.build())
        Log.d(TAG,"Foregroung has satrted")

        timeInSeconds.observe(this, Observer {
            val notification = currNotificationBuilder
                .setContentTitle(TrackingUtility.getFormattedStopWatchTime(it*1000L))
            notificationManager.notify(NOTIFICATION_ID,notification.build())

        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID,Constants.NOTIFICATION_CHANNEL_NAME,
                      IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }

}