package com.androidstrike.schoolprojects.securitygame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentSplashScreenBinding
import com.androidstrike.schoolprojects.securitygame.utils.Common.auth

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SplashScreen : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            txtSplash.animate().setDuration(2000).alpha(1f).withEndAction {


                if (auth.currentUser != null) {
                    // User is logged in
                    // Perform necessary actions
                    val navToHome = SplashScreenDirections.actionSplashScreenToHome2()
                    findNavController().navigate(navToHome)
                } else {
                    // User is not logged in
                    // Redirect to login screen or perform other actions
                    val navToSignIn = SplashScreenDirections.actionSplashScreenToSignIn()
                    findNavController().navigate(navToSignIn)
                }


            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}