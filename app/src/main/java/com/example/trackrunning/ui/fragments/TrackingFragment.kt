package com.example.trackrunning.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.trackrunning.R
import com.example.trackrunning.databinding.FragmentTrackingBinding
import com.example.trackrunning.other.Constants
import com.example.trackrunning.other.Constants.ACTION_PAUSE_SERVICE
import com.example.trackrunning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.trackrunning.other.Constants.ACTION_STOP_SERVICE
import com.example.trackrunning.other.Constants.MAP_ZOOM
import com.example.trackrunning.other.Constants.POLYLINE_COLOR
import com.example.trackrunning.other.Constants.POLYLINE_WIDTH
import com.example.trackrunning.other.TrackingUtility
import com.example.trackrunning.services.Polyline
import com.example.trackrunning.services.TrackingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking){

    private lateinit var binding: FragmentTrackingBinding
    private  var mp: GoogleMap? = null
    private var isServiceStart = false
    private var isStartingLocation = true
    private var isTracking = false
    private var pathsPoints = mutableListOf<Polyline>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         binding = FragmentTrackingBinding.bind(view)
         binding.mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        binding.mapView.getMapAsync {
            mp = it
            addAllPolylines()

        }
        subscribeToObservers()
    }
    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
                updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathsPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })
        TrackingService.timeInMillies.observe(viewLifecycleOwner, Observer {
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(it,true)
            binding.tvTimer.text = formattedTime
        })
    }
    private fun toggleRun() {
        if(isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if(!isTracking) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else {
            binding.btnToggleRun.text = "Stop"
            binding.btnFinishRun.visibility = View.GONE
        }
    }
    private fun moveCameraToUser(){
        if(pathsPoints.isNotEmpty() && pathsPoints.last().isNotEmpty()){
            mp?.animateCamera(CameraUpdateFactory.newLatLngZoom(pathsPoints.last().last(),MAP_ZOOM))

            // mp?.addMarker(MarkerOptions().position(pathsPoints.last().last()))



        }
    }
    private fun addAllPolylines(){

        for(polyline in pathsPoints){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            mp?.addPolyline(polylineOptions)
        }
    }
    private fun addLatestPolyline(){

        if(pathsPoints.isNotEmpty() && pathsPoints.last().size > 1){
            val preLastLatLong = pathsPoints.last()[pathsPoints.last().size - 2]
            val lastLatLng = pathsPoints.last()[pathsPoints.last().size-1]
            val polylineOptions = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)

                .add(preLastLatLong)
                .add(lastLatLng)
            mp?.addPolyline(polylineOptions)
        }

    }
    private fun sendCommandToService(action:String){
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }


    override fun onResume() {
        super.onResume()
        binding.mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView?.onSaveInstanceState(outState)
    }

}