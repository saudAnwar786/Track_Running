package com.example.trackrunning.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.trackrunning.R
import com.example.trackrunning.adapters.RunAdapter
import com.example.trackrunning.databinding.FragmentTrackingBinding
import com.example.trackrunning.db.Run
import com.example.trackrunning.other.Constants
import com.example.trackrunning.other.Constants.ACTION_PAUSE_SERVICE
import com.example.trackrunning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.trackrunning.other.Constants.ACTION_STOP_SERVICE
import com.example.trackrunning.other.Constants.MAP_ZOOM
import com.example.trackrunning.other.Constants.POLYLINE_COLOR
import com.example.trackrunning.other.Constants.POLYLINE_WIDTH
import com.example.trackrunning.other.TrackingUtility
import com.example.trackrunning.services.Polyline
import com.example.trackrunning.services.Polylines
import com.example.trackrunning.services.TrackingService
import com.example.trackrunning.ui.viewModels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking){

    private lateinit var binding: FragmentTrackingBinding
    private  var mp: GoogleMap? = null
    private var isServiceStart = false
    private var isStartingLocation = true
    private var isTracking = false
    private var pathsPoints = mutableListOf<Polyline>()
    private var currTimeInMillis =0L
    private var menu:Menu? = null

    @set:Inject
     var weight = 80f

    private val viewModel:MainViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         binding = FragmentTrackingBinding.bind(view)
         binding.mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
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
            currTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currTimeInMillis,true)
            binding.tvTimer.text = formattedTime
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.tracking_toolbar_menu,menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currTimeInMillis>0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.toolbarTracking->{
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)

    }
    private fun showCancelTrackingDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext(),R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are You sure to cancel the Run ")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){_,_->
                stopRun()
            }
            .setNegativeButton("No"){dialogINterface,_->
                dialogINterface.cancel()
            }
            .create()
        dialog.show()


    }
    private fun stopRun(){
        sendCommandToService(Constants.ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
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
        if(!isTracking && currTimeInMillis>0L) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else if(isTracking) {
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
    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.builder()
        for(polyline in pathsPoints){
            for(i in polyline){
                bounds.include(i)
            }
        }
        mp?.moveCamera(CameraUpdateFactory.newLatLngBounds(
            bounds.build(),
            binding.mapView.width,
            binding.mapView.height,
            (binding.mapView.height * 0.05f).toInt()

        ))
    }
    private fun endRunAndSaveToDb(){
        mp?.snapshot { bmp->
            var distanceInMeters = 0f
            for(polyline in pathsPoints){
                distanceInMeters+= TrackingUtility.calculatePolylineLength(polyline)
            }
            var avgSpeed = round((distanceInMeters/1000f)/currTimeInMillis/1000f / 3600 * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val calorieBurned = ((distanceInMeters/1000f)*weight).toInt()
            val run = Run(bmp,dateTimeStamp, avgSpeed,calorieBurned,currTimeInMillis,distanceInMeters)
             viewModel.insertRun(run)
             view?.let{ rootView ->
                 Snackbar.make(rootView, "Added Successfully", Snackbar.LENGTH_LONG).show()
             }
            stopRun()
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