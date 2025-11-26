package com.example.glowpoint.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.databinding.FragmentShopsBinding
import com.example.glowpoint.ui.viewmodel.ParentChildForShopsContainerViewModel
import com.example.glowpoint.ui.viewmodel.SharedForEachSalonViewModel
import com.example.glowpoint.ui.viewmodel.SharedForSearchShopsViewModel
import com.example.glowpoint.ui.viewmodel.ShopsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopsFragment : Fragment() {

    private lateinit var binding: FragmentShopsBinding

    private val viewModel: ShopsViewModel by viewModels()
    private val sharedForSearchShopsViewModel: SharedForSearchShopsViewModel by activityViewModels()
    private val parentChildForShopsContainerViewModel: ParentChildForShopsContainerViewModel by activityViewModels()
    private val sharedForEachSalonViewModel: SharedForEachSalonViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupObserves()
        replaceFragment(ShopsContainerFragment())
    }

    private fun setupObserves() {
        sharedForSearchShopsViewModel.listOfServiceId.observe(viewLifecycleOwner) {
            parentChildForShopsContainerViewModel.setParentShopsNavigate(it)
            sharedForEachSalonViewModel.onSetListOfServiceId(it?.flatMap { map -> map.keys }
                ?.filterNotNull())
        }

        sharedForEachSalonViewModel.isNavigate.observe(viewLifecycleOwner) {
            if (it) {
                val action = ShopsFragmentDirections.actionShopsFragmentToEachShopFragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager
            .beginTransaction()
            .replace(binding.childFragmentContainer.id, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedForSearchShopsViewModel.reset()
    }
}