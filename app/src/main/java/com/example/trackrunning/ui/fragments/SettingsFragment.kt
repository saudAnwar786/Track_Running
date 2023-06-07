package com.example.trackrunning.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.trackrunning.R
import com.example.trackrunning.databinding.FragmentSettingsBinding
import com.example.trackrunning.other.Constants
import com.example.trackrunning.other.Constants.KEY_NAME
import com.example.trackrunning.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var binding: FragmentSettingsBinding
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        loadFieldsFromSharedPref()
        binding.btnApplyChanges.setOnClickListener {
            if(saveChangesToSharedPreferences()){
                view?.let{
                    Snackbar.make(view,"Saved Changes Successfully",Snackbar.LENGTH_SHORT)
                        .show()
                }


            }else{
                Snackbar.make(view,"PLease both fields",Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

    }
    private fun loadFieldsFromSharedPref() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }
    private fun saveChangesToSharedPreferences():Boolean{
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
         if(name.isEmpty() || weight.isEmpty()) return false
        sharedPreferences.edit()
            .putString(Constants.KEY_NAME,name)
            .putFloat(Constants.KEY_WEIGHT,weight.toFloat())
            .apply()
        val toolBarText = "Let's go ${name}"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolBarText
        return true
    }

}