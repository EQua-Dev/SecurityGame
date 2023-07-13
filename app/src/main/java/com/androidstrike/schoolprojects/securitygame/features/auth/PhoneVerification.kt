package com.androidstrike.schoolprojects.securitygame.features.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentPhoneVerificationBinding
import com.androidstrike.schoolprojects.securitygame.utils.Common
import com.androidstrike.schoolprojects.securitygame.utils.Common.auth
import com.androidstrike.schoolprojects.securitygame.utils.toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneVerification : Fragment() {

    private var _binding: FragmentPhoneVerificationBinding? = null
    private val binding get() = _binding!!

    private val arg: PhoneVerificationArgs by navArgs()
    private lateinit var userPhoneNumber: String

    lateinit var sentCode: String

    val TAG = "PhoneVerification"



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPhoneVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPhoneNumber = arg.phoneNumber
        Log.d(TAG, "userPhoneNumber: $userPhoneNumber")

        val options = PhoneAuthOptions.newBuilder(Common.auth)
            .setPhoneNumber(userPhoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

//        binding.twoFaBtn.setOnClickListener {
//            verifyCode()
//        }


    }


    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            binding.twoFaBtn.setOnClickListener {
                val enteredCode = binding.phoneVerificationCode.text.toString().trim()
                if (enteredCode == credential.smsCode){
                    requireContext().toast("code valid")
                    val navToHome = PhoneVerificationDirections.actionPhoneVerificationToHome2()
                    findNavController().navigate(navToHome)
                }
                else{
                    requireContext().toast("code invalid")
                }
            }
            //signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")
            sentCode = verificationId

            verifyCode(sentCode)

            // Save verification ID and resending token so we can use them later
            //storedVerificationId = verificationId
            //resendToken = token
        }
    }

    private fun verifyCode(sentCode: String) {
        binding.twoFaBtn.setOnClickListener {
            val enteredCode = binding.phoneVerificationCode.text.toString().trim()
            val credential = PhoneAuthProvider.getCredential(sentCode, enteredCode)
            Log.d(TAG, "verifyCode: ${this.sentCode} $enteredCode ${credential.smsCode}")
            if (enteredCode == credential.smsCode){
                requireContext().toast("code valid")
                val navToHome = PhoneVerificationDirections.actionPhoneVerificationToHome2()
                findNavController().navigate(navToHome)
            }
            else{
                requireContext().toast("code invalid")
            }
        }

        //signInWithPhoneAuthCredential(credential)

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    requireContext().toast("Success")
                    // TODO: make it not to create a new user with the phone number
                    val navToHome = PhoneVerificationDirections.actionPhoneVerificationToHome2()
                    findNavController().navigate(navToHome)
                } else {
                    // Sign in failed, display a message and update the UI
                    requireContext().toast(task.exception.toString())
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                    requireContext().toast(task.exception.toString())
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}