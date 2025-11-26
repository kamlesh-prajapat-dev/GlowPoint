package com.example.glowpoint.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentSplashBinding
import com.example.glowpoint.ui.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            if (_binding != null) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.splashFragment, true)
                    .build()
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment, null, navOptions)
            }
        }, 2000)
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.isUserLoggedIn() && viewModel.getLocation().isNotEmpty()) {
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
        } else if (viewModel.isUserLoggedIn() && viewModel.getLocation().isEmpty()) {
            findNavController().navigate(R.id.action_splashFragment_to_locationFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}