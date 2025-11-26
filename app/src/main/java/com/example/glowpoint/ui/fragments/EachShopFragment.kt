package com.example.glowpoint.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.R
import com.example.glowpoint.data.models.SalonModel
import com.example.glowpoint.databinding.FragmentEachShopBinding
import com.example.glowpoint.ui.adapter.RecyclerViewItemAdapter
import com.example.glowpoint.ui.adapter.RecyclerViewTimeSlotAdapter
import com.example.glowpoint.ui.viewmodel.EachShopViewModel
import com.example.glowpoint.ui.viewmodel.SharedForEachSalonViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@AndroidEntryPoint
class EachShopFragment : Fragment() {

    private lateinit var binding: FragmentEachShopBinding
    private val viewModel: EachShopViewModel by viewModels()
    private val sharedViewModel: SharedForEachSalonViewModel by activityViewModels()

    private lateinit var itemAdapter: RecyclerViewItemAdapter
    private lateinit var timeSlotAdapter: RecyclerViewTimeSlotAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEachShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerViews() {
        itemAdapter = RecyclerViewItemAdapter(viewModel::toggleServiceSelection)
        binding.servicesRecycler.adapter = itemAdapter

        timeSlotAdapter = RecyclerViewTimeSlotAdapter(viewModel::toggleTimeSlotSelection)
        binding.timeSlotsRecycler.adapter = timeSlotAdapter
    }

    private fun setupObservers() {
        // This is the main observer that kicks everything off.
        // It waits for both the salon and the selected service IDs to be ready.
        sharedViewModel.salonWithSelection.observe(viewLifecycleOwner) { data ->
            if (data == null) return@observe
            setupShopDetails(data.salon)
            viewModel.loadSalonData(data.salon, data.selectedIds)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.isVisible = isLoading
            if (isLoading) {
                binding.servicesRecycler.isVisible = false
                binding.emptyStateGroup.isVisible = false
                binding.errorStateGroup.isVisible = false
            }
        }

        viewModel.showEmptyState.observe(viewLifecycleOwner) { showEmpty ->
            binding.emptyStateGroup.isVisible = showEmpty && !viewModel.isLoading.value!!
            binding.servicesRecycler.isVisible = !showEmpty
        }

        viewModel.errorState.observe(viewLifecycleOwner) { error ->
            val showError = error != null && !viewModel.isLoading.value!!
            binding.errorStateGroup.isVisible = showError
            if (showError) {
                binding.errorStateText.text = error
                binding.servicesRecycler.isVisible = false
            }
        }

        viewModel.salonServices.observe(viewLifecycleOwner) { services ->
            itemAdapter.submitList(services)
        }

        viewModel.timeSlots.observe(viewLifecycleOwner) { timeSlots ->
            timeSlotAdapter.submitList(timeSlots)
        }

        viewModel.isTimeSlotRecyclerViewVisible.observe(viewLifecycleOwner){
            binding.timeSlotsRecycler.isVisible = it
        }

        viewModel.isVisibleBookSlotsButton.observe(viewLifecycleOwner) {
            binding.bookSlotsButton.isVisible = it
        }
    }

    private fun setupClickListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            sharedViewModel.reset()
            findNavController().navigateUp()
        }
    }

    private fun setupShopDetails(salon: SalonModel) {
        binding.shopName.text = salon.name
        binding.shopAddress.text = salon.address
        binding.shopRatting.text = salon.rating.toString()
        binding.shopDistance.text = (salon.distance?.div(1000))?.let { "%.2f km away".format(it) } ?: ""

        val isOpen = isShopOpenNow(salon.openTime, salon.closeTime)
        val context = binding.root.context
        if (isOpen) {
            binding.shopStatus.text = "Open Now"
            binding.shopStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
        } else {
            binding.shopStatus.text = "Closed Now"
            binding.shopStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
        }
    }

    private fun isShopOpenNow(openTimeStr: String?, closeTimeStr: String?): Boolean {
        if (openTimeStr.isNullOrBlank() || closeTimeStr.isNullOrBlank()) return false
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val openTime = LocalTime.parse(openTimeStr, formatter)
            val closeTime = LocalTime.parse(closeTimeStr, formatter)
            val currentTime = LocalTime.now()
            !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime)
        } catch (e: DateTimeParseException) {
            android.util.Log.e("EachShopFragment", "Invalid time format", e)
            false
        }
    }

    override fun onDestroy() {
        sharedViewModel.reset()
        super.onDestroy()
    }
}
