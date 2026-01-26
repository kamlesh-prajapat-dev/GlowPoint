package com.example.glowpoint.ui.screens.sample

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.R
import com.example.glowpoint.data.sample.SampleData
import com.example.glowpoint.databinding.FragmentLocationBinding
import com.example.glowpoint.databinding.FragmentSampleDataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SampleDataFragment : Fragment() {
    private var _binding: FragmentSampleDataBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SampleDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSampleDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListener()
        observeViewModel()
    }

    private fun setupListener() {
        binding.addMenServicesBtn.setOnClickListener {
            val services = SampleData.getSampleMenServiceData()
            viewModel.saveMenServices(services)
        }

        binding.addWomenServicesBtn.setOnClickListener {
            val services = SampleData.getSampleWomenServiceData()
            viewModel.saveWomenServices(services)
        }

        binding.addShopBtn.setOnClickListener {
            val salonShops = SampleData.getSampleSalonShopData()
            viewModel.saveShopData(salonShops)
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when(it) {
                        SampleDataUIState.AlreadySaved -> {
                            Toast.makeText(requireContext(), "Data Already saved.", Toast.LENGTH_SHORT).show()
                            onSetLoading(false)
                        }
                        is SampleDataUIState.Failure -> {
                            Toast.makeText(requireContext(), it.e.message, Toast.LENGTH_SHORT).show()
                            onSetLoading(false)
                        }
                        SampleDataUIState.Idle -> {
                            onSetLoading(false)
                        }
                        SampleDataUIState.Loading -> {
                            onSetLoading(true)
                        }
                        SampleDataUIState.Success -> {
                            Toast.makeText(requireContext(), "Data Successfully saved.", Toast.LENGTH_SHORT).show()
                            onSetLoading(false)
                        }
                    }
                }
            }
        }
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}