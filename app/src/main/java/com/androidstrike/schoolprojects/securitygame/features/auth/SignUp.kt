package com.androidstrike.schoolprojects.securitygame.features.auth

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.securitygame.R
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentSignUpBinding
import com.androidstrike.schoolprojects.securitygame.models.UserData
import com.androidstrike.schoolprojects.securitygame.utils.Common.auth
import com.androidstrike.schoolprojects.securitygame.utils.Common.userCollectionRef
import com.androidstrike.schoolprojects.securitygame.utils.formatPhoneNumber
import com.androidstrike.schoolprojects.securitygame.utils.hashString
import com.androidstrike.schoolprojects.securitygame.utils.isPasswordValid
import com.androidstrike.schoolprojects.securitygame.utils.showProgressDialog
import com.androidstrike.schoolprojects.securitygame.utils.toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUp : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var username: String
    private lateinit var email: String
    private lateinit var phoneNumber: String
    private lateinit var password: String
    private lateinit var confirmPassword: String
    private lateinit var userCountryCode: String

    private var usernameOkay = false
    private var emailOkay = false
    private var phoneNumberOkay = false
    private var passwordOkay = false
    private var confirmPasswordOkay = false
    private var userCountryCodeOkay = false


    private var progressDialog: Dialog? = null

    private val TAG = "SignUp"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            userRegisterLoginPrompt.setOnClickListener {
                val navToLogIn = SignUpDirections.actionSignUpToSignIn()
                findNavController().navigate(navToLogIn)
            }

            binding.registerUserName.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    username = registerUserName.text.toString().trim()
                    validateUserName(username)
                }
            }
            binding.registerCustomerEmail.setOnFocusChangeListener { v, hasFocus ->
                val emailLayout = v as TextInputEditText
                email = emailLayout.text.toString().trim()
                if (!hasFocus) {
                    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        binding.textInputLayoutRegisterCustomerEmail.error =
                            resources.getString(R.string.email_invalid)
                    } else {
                        emailOkay = true
                        binding.textInputLayoutRegisterCustomerEmail.error = null
                    }
                }
            }
            binding.registerCustomerPhone.setOnFocusChangeListener { v, hasFocus ->
                val phoneLayout = v as TextInputEditText
                phoneNumber = phoneLayout.text.toString().trim()
                if (!hasFocus) {
                    if (phoneNumber.isEmpty() || phoneNumber.length < 10) {
                        binding.textInputLayoutRegisterCustomerPhone.error =
                            resources.getString(R.string.invalid_phone_number)
                    } else {
                        phoneNumberOkay = true
                        binding.textInputLayoutRegisterCustomerPhone.error = null
                    }
                }
            }
            binding.registerCustomerPassword.setOnFocusChangeListener { v, hasFocus ->
                val passwordLayout = v as TextInputEditText
                password = passwordLayout.text.toString().trim()
                if (!hasFocus) {
                    if (isPasswordValid(password)) {
                        binding.textInputLayoutRegisterCustomerPassword.error =
                            resources.getString(R.string.invalid_password)
                    } else {
                        passwordOkay = true
                        binding.textInputLayoutRegisterCustomerPassword.error = null
                    }
                }
            }
            binding.registerCustomerConfirmPassword.setOnFocusChangeListener { v, hasFocus ->
                val confirmPasswordLayout = v as TextInputEditText
                confirmPassword = confirmPasswordLayout.text.toString().trim()
                if (!hasFocus) {
                    if (confirmPassword != password) {
                        binding.textInputLayoutRegisterCustomerConfirmPassword.error =
                            resources.getString(R.string.invalid_confirm_password)
                    } else {
                        confirmPasswordOkay = true
                        binding.textInputLayoutRegisterCustomerConfirmPassword.error = null
                    }
                }
            }

            userCountryCode = countryCodePicker.defaultCountryCodeWithPlus
            countryCodePicker.setOnCountryChangeListener {
                userCountryCode = countryCodePicker.selectedCountryCodeWithPlus
                // Handle selected country code and name
                countryCodePicker.setNumberAutoFormattingEnabled(true)
            }

            userRegisterBtn.setOnClickListener {
                if (emailOkay && phoneNumberOkay && passwordOkay && confirmPasswordOkay){
                    username = registerUserName.text.toString().trim()
                    email = registerCustomerEmail.text.toString().trim()
                    phoneNumber = "$userCountryCode${registerCustomerPhone.text.toString().trim()}"
                    password = registerCustomerPassword.text.toString().trim()
                    confirmPassword = registerCustomerConfirmPassword.text.toString().trim()

                    createUser(email, password)
                }else{
                    requireContext().toast(resources.getString(R.string.missing_fields))
                }


                //validateInput()

            }
        }

    }

    private fun validateInput() {

        with(binding) {
            if (username.isEmpty() ){//|| validateUserName(username)) {
                textInputLayoutRegisterUserName.error =
                    resources.getString(R.string.username_invalid)
                return
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                textInputLayoutRegisterCustomerEmail.error =
                    resources.getString(R.string.email_invalid)
                return
            }
            if (phoneNumber.isEmpty()) {
                textInputLayoutRegisterCustomerPhone.error =
                    resources.getString(R.string.invalid_email)
                return
            }
            if (!isPasswordValid(password)) {
                textInputLayoutRegisterCustomerPassword.error =
                    resources.getString(R.string.invalid_password)
                return
            }
            if (confirmPassword != password) {
                textInputLayoutRegisterCustomerConfirmPassword.error =
                    resources.getString(R.string.invalid_confirm_password)
                return
            } else {
                createUser(email, password)
            }
        }

    }

    private fun createUser(email: String, password: String) {


        val mAuth = FirebaseAuth.getInstance()
        showProgress()
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val newUserId = mAuth.uid
                    val user = mAuth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                requireContext().toast(resources.getString(R.string.email_sent))
                                launchVerifyEmailDialog()
                            }
                        }
                } else {
                    it.exception?.message?.let { message ->
                        hideProgress()
                        requireActivity().toast(message)
                    }
                }
            }
    }


    private fun launchVerifyEmailDialog() {


        val builder =
            layoutInflater.inflate(R.layout.email_verification_dialog, null)

        val userEmail = builder.findViewById<TextView>(R.id.email_verification_email)

        val btnLinkClicked =
            builder.findViewById<Button>(R.id.email_verification_btn)

        userEmail.text = auth.currentUser?.email
        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()


        btnLinkClicked.setOnClickListener {
            auth.currentUser!!.reload().addOnCompleteListener { task ->
                Log.d(TAG, "launchVerifyEmailDialog: ${auth.currentUser?.email}")
                Log.d(TAG, "launchVerifyEmailDialog: ${auth.currentUser!!.isEmailVerified}")

                if (task.isSuccessful) {
                    if (auth.currentUser!!.isEmailVerified) {
                        val userData = UserData(
                            username = username,
                            email = hashString(email),
                            phoneNumber = formatPhoneNumber(phoneNumber),
                            userId = auth.uid.toString()
                        )
                        dialog.dismiss()
                        saveUser(userData)
                    } else {
                        hideProgress()
                        requireContext().toast(resources.getString(R.string.email_not_verified))
                    }
                } else {
                    requireContext().toast(task.exception.toString())
                }

            }

        }



        dialog.show()

    }


    private fun saveUser(userData: UserData) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userCollectionRef.document(userData.userId).set(userData).await()
                hideProgress()
                val navToSignIn = SignUpDirections.actionSignUpToSignIn()
                findNavController().navigate(navToSignIn)
                //sendVerificationEmail(auth.currentUser!!)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    activity?.toast(e.message.toString())
                }
            }


        }

    private fun sendVerificationEmail(user: FirebaseUser) {

//        val actionCodeSettings = actionCodeSettings {
//            // URL you want to redirect back to. The domain (www.example.com) for this
//            // URL must be whitelisted in the Firebase Console.
//            url = "https://securitygame.page.link/testsecguard"
//            // This must be true
//            handleCodeInApp = true
//            setIOSBundleId(requireContext().packageName)
//            setAndroidPackageName(
//                requireContext().packageName,
//                true, // installIfNotAvailable
//                "12", // minimumVersion
//            )
//        }

        user.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (user.isEmailVerified) {
                        // User is logged in and email is verified

                    } else {
                        // User is not logged in or email is not verified
                        requireContext().toast("Email not verified")
                    }
                } else {
                    val exception = task.exception
                    requireContext().toast(exception.toString())
                }

            }

//        auth.sendSignInLinkToEmail(email, actionCodeSettings)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    requireContext().toast(resources.getString(R.string.check_email))
//                    Log.d(TAG, "Email sent.")
//                    val navToSignIn = SignUpDirections.actionSignUpToSignIn()
//                    findNavController().navigate(navToSignIn)
//
//                }
//                else{
//                    Log.d(TAG, "sendVerificationEmail: ${task.exception}")
//                }
//            }
    }


    private fun showProgress() {
        hideProgress()
        progressDialog = requireActivity().showProgressDialog()
    }

    private fun hideProgress() {
        progressDialog?.let { if (it.isShowing) it.cancel() }
    }

    private fun validateUserName(username: String) {
        //var usernameIsAvailable: Boolean
        showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            userCollectionRef
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                    for (document in querySnapshot.documents) {
                        val item = document.toObject(UserData::class.java)
                        if (item?.username == username) {
                            binding.textInputLayoutRegisterUserName.error =
                                resources.getString(R.string.user_name_exists, username)
                            usernameOkay = false
                            //usernameIsAvailable = false
                        }
                        else{

                            binding.textInputLayoutRegisterUserName.error =
                                null
                            usernameOkay = true
                        }
                    }
                    hideProgress()
                }
        }
        //return usernameIsAvailable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}