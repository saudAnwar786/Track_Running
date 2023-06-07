package com.example.trackrunning.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.trackrunning.R
import com.example.trackrunning.databinding.FragmentSettingsBinding
import com.example.trackrunning.databinding.FragmentSetupBinding
import com.example.trackrunning.other.Constants
import com.example.trackrunning.other.Constants.KEY_NAME
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment: Fragment(R.layout.fragment_setup) {

    private lateinit var binding:FragmentSetupBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetupBinding.bind(view)

        if(!isFirstAppOpen){
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment,true)
                .build()

            val name = sharedPreferences.getString(KEY_NAME,"")?:""
            val toolbarText = "Let's go ${name}"
            requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolbarText

            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment2,
                savedInstanceState,
                navOptions
            )
        }
        binding.tvContinue.setOnClickListener {

            if(writePersonaldataToSharedPreferences()) {

                findNavController().navigate(R.id.action_setupFragment_to_runFragment2)
            }else{
                view?.let {
                    Snackbar.make(it,"Please enter your details",Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun writePersonaldataToSharedPreferences():Boolean{
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty() ) return false
        sharedPreferences.edit()
            .putString(Constants.KEY_NAME,"${name}")
            .putFloat(Constants.KEY_WEIGHT,weight.toFloat())
            .putBoolean(Constants.KEY_FIRST_TIME_TOGGLE,false)
            .apply()

        val toolbarText = "Let's go ${name}"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolbarText
        return true
    }
}