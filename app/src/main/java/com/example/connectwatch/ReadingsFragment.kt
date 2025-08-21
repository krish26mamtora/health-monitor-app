package com.example.connectwatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.connectwatch.databinding.FragmentReadingsBinding

class ReadingsFragment : Fragment() {
    private var _binding: FragmentReadingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReadingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
        binding.sensorGrid.startAnimation(fadeIn)
        binding.tvBPM.text = "75 BPM"
        binding.tvOxygen.text = "98 %"
        binding.tvTemperature.text = "36.7 °C"
        binding.tvSteps.text = "1,254 Steps"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
