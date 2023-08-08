package com.androidstrike.schoolprojects.securitygame.features.quiz

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.androidstrike.schoolprojects.securitygame.R
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentQuizDoneBinding
import com.androidstrike.schoolprojects.securitygame.models.GameDetails
import com.androidstrike.schoolprojects.securitygame.models.Leaderboard
import com.androidstrike.schoolprojects.securitygame.models.UserData
import com.androidstrike.schoolprojects.securitygame.utils.Common
import com.androidstrike.schoolprojects.securitygame.utils.Common.GAME_DETAILS_REF
import com.androidstrike.schoolprojects.securitygame.utils.Common.auth
import com.androidstrike.schoolprojects.securitygame.utils.Common.leaderBoardCollectionRef
import com.androidstrike.schoolprojects.securitygame.utils.Common.userCollectionRef
import com.androidstrike.schoolprojects.securitygame.utils.enable
import com.androidstrike.schoolprojects.securitygame.utils.showProgressDialog
import com.androidstrike.schoolprojects.securitygame.utils.toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.Locale
import kotlin.properties.Delegates

class QuizDone : Fragment() {

    private var _binding: FragmentQuizDoneBinding? = null
    private val binding get() = _binding!!

    private val args: QuizDoneArgs by navArgs()
    private lateinit var quizScore: String
    private lateinit var highScore: String
    private lateinit var difficulty: String
    private lateinit var answeredAnswers: String
    private lateinit var totalQuestions: String
    private lateinit var correctAnswered: String

    private var localHighScore = 0

    private var progressDialog: Dialog? = null

    private var isHighScore: Boolean = false

    private val locationPermissionCode = 101

    private lateinit var request: String

    val TAG = "QuizDone"

    private var orderedLeaderBoardMap: MutableMap<String, Int> = mutableMapOf()




    private var loggedInUser = UserData()
    private var userGameDetails = GameDetails()

    private var orderNumber by Delegates.notNull<Int>()

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    lateinit var lastPlayed: String
    lateinit var newHighScore: String
    lateinit var lastScore: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentQuizDoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        quizScore = args.score
        answeredAnswers = args.answeredQuestions
        totalQuestions = args.totalQuestions
        difficulty = args.difficulty
        correctAnswered = args.correctAnswers

        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())


        with(binding) {

            gameScore.text = resources.getString(R.string.game_score, quizScore)
            textAttemptedQuestions.text = resources.getString(
                R.string.text_attempted_questions,
                answeredAnswers,
                totalQuestions
            )
            textCorrectQuestions.text = correctAnswered

            getUserGameDetails()

            quizDoneBtn.enable(false)
            quizDoneRetry.enable(false)

            quizDoneRetry.setOnClickListener {
                request = "replay"
                updateGameDetails()
            }

            quizDoneBtn.setOnClickListener {
                request = "done"
                updateGameDetails()
            }
        }

    }

    private fun getLeaderboard(loggedUser: UserData) {

        CoroutineScope(Dispatchers.IO).launch {

            leaderBoardCollectionRef.document(difficulty)
                .get()
                .addOnSuccessListener { document: DocumentSnapshot ->
                    val item = document.toObject(Leaderboard::class.java)
                    val mLeaderBoardMap = item?.ranks?.toMutableMap()

                    Log.d(TAG, "getLeaderboard: $mLeaderBoardMap")

                    val user = loggedUser.username
                    val userScore = quizScore.toInt()


                    //mLeaderBoardMap!![user] = userScore
                    Log.d(TAG, "getLeaderboard: $mLeaderBoardMap")

                    //get the position of the current score on the leaderboard
                    val scores: MutableList<Int> = mLeaderBoardMap!!.values.toMutableList()
                    scores.add(userScore)
                    val orderedScores = scores.sortedDescending()
                    val currentScoreRank = orderedScores.indexOf(userScore) + 1

                    if (mLeaderBoardMap.keys.contains(user)) { //if user is already on leaderboard
                        for (rank in mLeaderBoardMap) {
                            if (rank.key == user) {
                                if (rank.value < userScore) {
                                    mLeaderBoardMap[user] = userScore
                                    Log.d(TAG, "getLeaderboard: yes$rank")
                                    orderedLeaderBoardMap =
                                        mLeaderBoardMap
                                } else {
                                    Log.d(TAG, "getLeaderboard: no$rank")
                                    Log.d(TAG, "getLeaderboard: no$mLeaderBoardMap")

                                    orderedLeaderBoardMap =
                                        mLeaderBoardMap

                                }
                            }
                        }
                    } else {
                        mLeaderBoardMap[user] = userScore
                        orderedLeaderBoardMap = mLeaderBoardMap
                    }

                    //check if the current score is higher than the score on the leaderboard then add it


                    //Log.d(TAG, "orderedLeaderBoardMap: $orderedLeaderBoardMap")

                    orderNumber =
                        mLeaderBoardMap.toSortedMap(compareByDescending { mLeaderBoardMap[it] }).keys.indexOf(
                            user
                        ) + 1

//                    binding.textLeaderBoardRank.text = orderNumber.toString()
                    binding.textLeaderBoardRank.text = currentScoreRank.toString()
                    binding.quizDoneBtn.enable(true)
                    binding.quizDoneRetry.enable(true)


                }

        }


    }

    private fun getUserGameDetails() {

        CoroutineScope(Dispatchers.IO).launch {
            userCollectionRef.document(auth.uid.toString()).collection(GAME_DETAILS_REF).document(
//            Common.userCollectionRef.document("ElE9dfN1rXVaJ0MD8IsJv046BnV2")
                //.collection(Common.GAME_DETAILS_REF).document(
                difficulty
            )
                //.document(difficulty)// whereEqualTo("employerBusiness", loggedInManager.employerBusiness)
                .get()
                .addOnSuccessListener { document: DocumentSnapshot ->
                    val item = document.toObject(GameDetails::class.java)
                    if (item != null) {
                        userGameDetails = item
                    }

                    Log.d(
                        "QuizDone",
                        "getUserGameDetails: $quizScore, ${userGameDetails.highScore}"
                    )


                    if (userGameDetails.highScore.isNotEmpty())
                        localHighScore = userGameDetails.highScore.toInt()
                    binding.highScore.apply {
                        if (quizScore.toInt() > localHighScore) {
                            isHighScore = true
                            text = resources.getString(R.string.game_score, quizScore)
                            append(" (new)")
                        } else {
                            isHighScore = false
                            text =
                                resources.getString(R.string.game_score, userGameDetails.highScore)

                        }
                    }
                    getUser()
                }

        }

    }


    private fun getUser() {
        var loggedUser = UserData()
        CoroutineScope(Dispatchers.IO).launch {
            //Common.userCollectionRef.whereEqualTo("userId", Common.auth.uid.toString())
            Common.userCollectionRef.document(auth.uid!!)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        requireContext().toast(error.message.toString())
                        return@addSnapshotListener
                    }
                    if (value != null && value.exists()) {
                        loggedUser = value.toObject(UserData::class.java)!!
                        getLeaderboard(loggedUser)
                    }
                }
//            get()
//                .addOnSuccessListener { querySnapshot: QuerySnapshot ->
//
//                    for (document in querySnapshot.documents) {
//                        val item = document.toObject(UserData::class.java)
//                        if (item?.userId == Common.auth.uid.toString())
//                            loggedUser = item
//                    }
//                    Log.d(TAG, "getUser: $loggedUser")
//                    getLeaderboard(loggedUser)
//                }
        }

    }


    private fun updateGameDetails() {
        showProgress()
        //update the last played date
        lastPlayed = System.currentTimeMillis().toString()
        //update the location played
        //val locationPlayed = getCurrentLocation()
        //update that this difficulty has been played
        //update the high score (will be changed if new and same if not)

        newHighScore = if (quizScore.toInt() > localHighScore) {

            quizScore
        } else
            userGameDetails.highScore
        //update the score gotten
        lastScore = quizScore
        //fetch the last played location
        checkLocationPermission()



    }

    private fun checkLocationPermission(

    ) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, you can request location updates.
            getLocation()
        } else {
            // Request location permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
    }

    private fun updateLeaderBoard(request: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val leaderBoardRef =
                //userCollectionRef.document(auth.uid.toString()).collection(GAME_DETAILS_REF)
                leaderBoardCollectionRef.document(difficulty)

            //val updates = hashMapOf<String, Any>()
            //for (rank in orderedLeaderBoardMap)

            //LEADER_BOARD_RANKS to orderedLeaderBoardMap
            val leaderboardRank = Leaderboard(
                ranks = orderedLeaderBoardMap.toMap()
            )

            Log.d(TAG, "updateLeaderBoard: ${orderedLeaderBoardMap.toMap()}")



            leaderBoardRef.set(leaderboardRank)
                .addOnSuccessListener {
                    // Update successful
                    requireContext().toast("Update successful")
                    hideProgress()
                    if (request == "replay") {
                        //nav to rules page
                        val navToRules = QuizDoneDirections.actionQuizDoneToRules(difficulty)
                        findNavController().navigate(navToRules)
                    } else {
                        //nav to home page
                        val navToHome = QuizDoneDirections.actionQuizDoneToHome2()
                        findNavController().navigate(navToHome)
                    }


                    Log.d("QuizDone", "updateGameDetails: Updated")

                }
                .addOnFailureListener { e ->
                    // Handle error
                    hideProgress()
                    Log.d(TAG, "updateGameDetails: ${e.toString()}")
                    requireContext().toast(e.message.toString())
                }


        }

    }


    // Override onRequestPermissionsResult to handle the permission request result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can request location updates.
                getLocation()
            } else {
                // Permission denied, handle this case as needed.
                // For example, show a message to the user or disable location functionality.
                requireContext().toast("Location permission denied. Cannot fetch location")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val geocoder: Geocoder = Geocoder(requireContext(), Locale.getDefault())


        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Use the location object to get latitude and longitude.
                val latitude = location.latitude
                val longitude = location.longitude

                var lastLocationPlayed = ""

                try {
                    val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address: Address = addresses[0]
                        val sb = StringBuilder()
                        for (i in 0..address.maxAddressLineIndex) {
                            sb.append(address.getAddressLine(i)).append(" ")
                        }
                        lastLocationPlayed = sb.toString().trim()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                //update the new list of leaderboard map that has been sorted
                CoroutineScope(Dispatchers.IO).launch {
                    val orderRef =
                        userCollectionRef.document(auth.uid.toString()).collection(GAME_DETAILS_REF)
                            .document(
//                Common.userCollectionRef.document("ElE9dfN1rXVaJ0MD8IsJv046BnV2")
//                    .collection(Common.GAME_DETAILS_REF).document(
                                difficulty
                            )

                    val updates = GameDetails(
                        lastPlayed = lastPlayed,
                        lastScore = lastScore,
                        highScore = newHighScore,
                        leaderBoardRank = orderNumber.toString(),
                        lastLocationPlayed = lastLocationPlayed,
                        hasPlayed = true
                    )

//            val updates = hashMapOf<String, Any>(
//                GAME_DETAILS_HIGH_SCORE to newHighScore,
//                GAME_DETAILS_LAST_PLAYED to lastPlayed,
//                GAME_DETAILS_LAST_SCORE to lastScore,
//                //GAME_DETAILS_LAST_LOCATION_PLAYED to locationPlayed,
//                GAME_DETAILS_LAST_LEADER_BOARD_RANK to orderNumber.toString(),
//                GAME_DETAILS_HAS_PLAYED to true
//                //"actualTime" to if (deliveryManStaff) {actualPreparationTime} else orderModel.actualTime
//            )

                    orderRef.set(updates).await()
                    //.addOnSuccessListener {
                    // Update successful
                    //requireContext().toast("Assignment successful")
                    //  hideProgress()
                    updateLeaderBoard(request)

                    Log.d("QuizDone", "updateGameDetails: Updated")

//                }
//                .addOnFailureListener { e ->
//                    // Handle error
//                    hideProgress()
//                    Log.d(TAG, "updateGameDetails: ${e.toString()}")
//                    requireContext().toast(e.message.toString())
//                }


                }

                //navigateToLocation(latitude, longitude)

                // Now you have the current location. You can use it as needed.
                // For example, show it on a map or send it to your server.
            } else {
                // Location is null, handle this case as needed.
                // For example, show an error message or request location updates.
            }
        }.addOnFailureListener { exception: Exception ->
            // Handle the failure to get the location.
            // For example, show an error message or request location updates.
            requireContext().toast(exception.message.toString())
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