package com.example.trackrunning.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.trackrunning.R
import com.example.trackrunning.databinding.FragmentStatisticsBinding
import com.example.trackrunning.other.TrackingUtility
import com.example.trackrunning.ui.viewModels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Math.round

@AndroidEntryPoint
class StatisticsFragment: Fragment(R.layout.fragment_statistics) {

    private lateinit var binding:FragmentStatisticsBinding

    private val viewModel:StatisticsViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStatisticsBinding.bind(view)
        subscribeToObservers()
    }
    private fun subscribeToObservers() {
        viewModel.totalTime.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                binding.tvTotalTime.text = totalTimeRun
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                binding.tvTotalDistance.text = totalDistanceString
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                binding.tvAverageSpeed.text = avgSpeedString
            }
        })
        viewModel.totalCalories.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories = "${it}kcal"
                binding.tvTotalCalories.text = totalCalories
            }
        })
//        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
//            it?.let {
//                val allAvgSpeeds = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
//                val bardataSet = BarDataSet(allAvgSpeeds, "Avg Speed Over Time").apply {
//                    valueTextColor = Color.WHITE
//                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
//                }
//                barChart.data = BarData(bardataSet)
//                barChart.marker = CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
//                barChart.invalidate()
//            }
//        })
    }
}