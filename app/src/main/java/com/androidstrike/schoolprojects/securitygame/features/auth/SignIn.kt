package com.androidstrike.schoolprojects.securitygame.features.auth

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentSignInBinding
import com.androidstrike.schoolprojects.securitygame.models.UserData
import com.androidstrike.schoolprojects.securitygame.utils.Common.auth
import com.androidstrike.schoolprojects.securitygame.utils.Common.userCollectionRef
import com.androidstrike.schoolprojects.securitygame.utils.enable
import com.androidstrike.schoolprojects.securitygame.utils.showProgressDialog
import com.androidstrike.schoolprojects.securitygame.utils.toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.QuerySnapshot
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
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

    private lateinit var mFusedLocationClient: FusedLocationProviderClient



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

        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())


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

                    //save user login location
                    //val loginLocation = getCurrentLocation()

                    val navToPhoneVerification =
                        SignInDirections.actionSignInToPhoneVerification(loggedInUser.phoneNumber)
                    findNavController().navigate(navToPhoneVerification)

                }
        }

    }

    private fun requestLocationPermissions() {
        //Request permissions
        Dexter.withActivity(requireActivity()) //Dexter makes runtime permission easier to implement
            .withPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    getCurrentLocation()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    requireContext().toast("Accept Permission")
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }
            }).check()
        //Request permissions
        Dexter.withActivity(requireActivity()) //Dexter makes runtime permission easier to implement
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    requireContext().toast("Accept Permission")
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }
            }).check()

    }

    private fun getCurrentLocation(): String {
        var currentLocation = "No Address Found!"

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestLocationPermissions()
        }
        mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
            val location: Location? = task.result


            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val list: List<Address> =
                geocoder.getFromLocation(location!!.latitude, location.longitude, 1)!!

            //mUsageLocality = "Locality\n${list[0].locality}"
            currentLocation = list[0].subLocality// .getAddressLine(0)

        }
        return currentLocation

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