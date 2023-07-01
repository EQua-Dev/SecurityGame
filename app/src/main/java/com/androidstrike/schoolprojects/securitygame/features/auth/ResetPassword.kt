package com.androidstrike.schoolprojects.securitygame.features.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentResetPasswordBinding
import com.androidstrike.schoolprojects.securitygame.utils.toast
import com.google.firebase.auth.FirebaseAuth

class ResetPassword : Fragment(){

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    lateinit var userEmail: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountForgotPasswordBtnResetPassword.setOnClickListener {
            userEmail = binding.forgotPasswordEmail.text.toString()


            FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Password reset email sent successfully
                        requireContext().toast("Password reset email sent")
                        val navToSignIn = ResetPasswordDirections.actionResetPasswordToSignIn()
                        findNavController().navigate(navToSignIn)
                    } else {
                        // Error occurred while sending password reset email
                        requireContext().toast("Failed to send password reset email")
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}