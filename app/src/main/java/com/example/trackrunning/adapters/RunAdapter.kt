package com.example.trackrunning.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.bumptech.glide.Glide
import com.example.trackrunning.databinding.ItemRunBinding
import com.example.trackrunning.db.Run
import com.example.trackrunning.other.TrackingUtility
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RunAdapter:RecyclerView.Adapter<RunAdapter.RunViewHolder>(){
    inner class RunViewHolder(val binding:ItemRunBinding):RecyclerView.ViewHolder(binding.root)



    val differCallBack = object : DiffUtil.ItemCallback<Run>(){
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return newItem.id == oldItem.id

        }
    }
    var differ = AsyncListDiffer(this,differCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            ItemRunBinding.inflate(LayoutInflater.from(parent.context)
            ,parent,false)
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val currItem = differ.currentList[position]
        holder.binding.apply {
            Glide.with(root.context).load(currItem.image).into(ivRunImage)
            tvAvgSpeed.text = currItem.avgSpeedInKmPerHour.toString() +"km/h"
            tvCalories.text = currItem.calorieBurned.toString() + "kCal"
           // tvDate.text = currItem.timeStamp.toString()
            tvTime.text = TrackingUtility.getFormattedStopWatchTime(
                currItem.timeInMillies
            )
            tvDistance.text = "${currItem.distanceInMeter /1000f}km"
            val calendar = Calendar.getInstance().apply {
                timeInMillis = currItem.timeStamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
             tvDate.text = dateFormat.format(calendar.time)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}