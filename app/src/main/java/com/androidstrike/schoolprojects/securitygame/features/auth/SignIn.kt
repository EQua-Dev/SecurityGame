package com.androidstrike.schoolprojects.securitygame.features.auth

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentSignInBinding
import com.androidstrike.schoolprojects.securitygame.models.UserData
import com.androidstrike.schoolprojects.securitygame.utils.Common.auth
import com.androidstrike.schoolprojects.securitygame.utils.Common.userCollectionRef
import com.androidstrike.schoolprojects.securitygame.utils.enable
import com.androidstrike.schoolprojects.securitygame.utils.showProgressDialog
import com.androidstrike.schoolprojects.securitygame.utils.toast
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SignIn : Fragment() {

    private var _binding: FragmentSignInBinding? = null

    private lateinit var email: String
    private lateinit var password: String

    lateinit var loggedInUser: UserData

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var progressDialog: Dialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loggedInUser = UserData()

        with(binding) {
            accountLogInCreateAccount.setOnClickListener {
                val navToSignUp = SignInDirections.actionSignInToSignUp()
                findNavController().navigate(navToSignUp)
            }

            accountLogInForgotPasswordPrompt.setOnClickListener {
                val navToResetPassword = SignInDirections.actionSignInToResetPassword()
                findNavController().navigate(navToResetPassword)
            }


            accountLogInBtnLogin.enable(false)

            signInPassword.addTextChangedListener {
                email = signInEmail.text.toString().trim()
                password = it.toString().trim()
                binding.accountLogInBtnLogin.apply {
                    enable(email.isNotEmpty() && password.isNotEmpty())
                    setOnClickListener {
                        login(email, password)
                    }
                }
            }

        }
    }

    private fun login(email: String, password: String) {

        showProgress()

        email.let { auth.signInWithEmailAndPassword(it, password) }
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            withContext(Dispatchers.Main) {
//                                pbLoading.visible(false)
                                hideProgress()
                                getUser()
                            }


                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                hideProgress()
                                requireActivity().toast(e.message.toString())
                            }
                        }
                    }
                } else {
                    //pbLoading.visible(false)
                    hideProgress()
                    activity?.toast(it.exception?.message.toString())
                }
            }

    }

    private fun getUser() {
        CoroutineScope(Dispatchers.IO).launch {
            userCollectionRef.whereEqualTo("userId", auth.uid.toString())
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                    for (document in querySnapshot.documents) {
                        val item = document.toObject(UserData::class.java)
                        if (item?.userId == auth.uid.toString())
                            loggedInUser = item
                    }
                    val navToPhoneVerification =
                        SignInDirections.actionSignInToPhoneVerification(loggedInUser.phoneNumber)
                    findNavController().navigate(navToPhoneVerification)

                }
        }

    }

    private fun showProgress() {
        hideProgress()
        progressDialog = requireActivity().showProgressDialog()
    }

    private fun hideProgress() {
        progressDialog?.let { if (it.isShowing) it.cancel() }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}