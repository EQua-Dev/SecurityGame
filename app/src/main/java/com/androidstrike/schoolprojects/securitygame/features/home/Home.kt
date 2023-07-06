package com.androidstrike.schoolprojects.securitygame.features.home

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.securitygame.R
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentHomeBinding
import com.androidstrike.schoolprojects.securitygame.models.GameDetails
import com.androidstrike.schoolprojects.securitygame.models.Leaderboard
import com.androidstrike.schoolprojects.securitygame.models.SecurityTips
import com.androidstrike.schoolprojects.securitygame.models.UserData
import com.androidstrike.schoolprojects.securitygame.utils.Common
import com.androidstrike.schoolprojects.securitygame.utils.Common.DIFFICULTY_EASY
import com.androidstrike.schoolprojects.securitygame.utils.Common.DIFFICULTY_HARD
import com.androidstrike.schoolprojects.securitygame.utils.Common.DIFFICULTY_MEDIUM
import com.androidstrike.schoolprojects.securitygame.utils.Common.GAME_DETAILS_REF
import com.androidstrike.schoolprojects.securitygame.utils.Common.securityTipsCollectionRef
import com.androidstrike.schoolprojects.securitygame.utils.showProgressDialog
import com.androidstrike.schoolprojects.securitygame.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var securityTipsAdapter: FirestoreRecyclerAdapter<SecurityTips, HomeBannerAdapter>? =
        null

    val TAG = "Home"
    private var progressDialog: Dialog? = null


    private var slideTimer: Timer? = null
    private var slideRunnable: TimerTask? = null

    //private var loggedInUser = UserData()
    //private lateinit var userGameDetails: GameDetails

    private var userEasyGameDetails = GameDetails()
    private var userMediumGameDetails = GameDetails()
    private var userHardGameDetails = GameDetails()

    private lateinit var difficulties: Array<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        getUser()

        difficulties = resources.getStringArray(R.array.difficulties)
        for (difficulty in difficulties) {
            getUserGameDetails(difficulty)
        }


    }

    private fun getRealtimeSecurityTips() {

        val securityTips =
            securityTipsCollectionRef
        val options = FirestoreRecyclerOptions.Builder<SecurityTips>()
            .setQuery(securityTips, SecurityTips::class.java).build()
        try {
            securityTipsAdapter = object :
                FirestoreRecyclerAdapter<SecurityTips, HomeBannerAdapter>(options) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): HomeBannerAdapter {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_security_tips_layout, parent, false)
                    return HomeBannerAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: HomeBannerAdapter,
                    position: Int,
                    model: SecurityTips
                ) {
                    holder.bannerTextviewHeading.text = model.heading
                    holder.bannerTextview.text = model.text

                }
            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        securityTipsAdapter?.startListening()
        binding.securityTipsBanner.adapter = securityTipsAdapter

        slideTimer = Timer()
        slideRunnable = object : TimerTask() {
            override fun run() {
                // Increment the current item index or reset to 0
                val currentItem = binding.securityTipsBanner.currentItem
                val nextItem =
                    if (currentItem == securityTipsAdapter!!.itemCount - 1) 0 else currentItem + 1
                binding.securityTipsBanner.setCurrentItem(nextItem, true)
            }
        }

// Schedule the slideRunnable to run every few seconds
        slideTimer?.scheduleAtFixedRate(slideRunnable, 3000, 3000)

    }


    private fun getUserGameDetails(difficulty: String) {//: GameDetails {
        showProgress()


        var loggedUser = UserData()
        CoroutineScope(Dispatchers.IO).launch {
            //Common.userCollectionRef.whereEqualTo("userId", Common.auth.uid.toString())
            Common.userCollectionRef.document("ElE9dfN1rXVaJ0MD8IsJv046BnV2").collection(
                GAME_DETAILS_REF
            ).document(difficulty)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        requireContext().toast(error.message.toString())
                        return@addSnapshotListener
                    }
                    if (value != null && value.exists()) {
                        val userGameDetails = value.toObject(GameDetails::class.java)!!
                        when (difficulty) {
                            DIFFICULTY_EASY -> {
                                userEasyGameDetails = userGameDetails
                                binding.easyHighScore.text = userEasyGameDetails.highScore.ifEmpty {
                                    "N/A"
                                }
                            }

                            DIFFICULTY_MEDIUM -> {
                                userMediumGameDetails = userGameDetails
                                binding.mediumHighScore.text =
                                    userMediumGameDetails.highScore.ifEmpty {
                                        "N/A"
                                    }
                            }

                            DIFFICULTY_HARD -> {
                                userHardGameDetails = userGameDetails
                                binding.hardHighScore.text = userHardGameDetails.highScore.ifEmpty {
                                    "N/A"
                                }
                            }
                        }
                        //getLeaderboard(loggedUser)
                        setCardClicks()
                        hideProgress()
                        //onResume()
                    }
                }
        }

        //var userGameDetails = GameDetails()
//        CoroutineScope(Dispatchers.IO).launch {
//            //userCollectionRef.document(auth.uid.toString()).collection(GAME_DETAILS_REF)
//            Common.userCollectionRef.document("ElE9dfN1rXVaJ0MD8IsJv046BnV2")
//                .collection(Common.GAME_DETAILS_REF).document(
//                DIFFICULTY_EASY
//            )
//                //.document(difficulty)// whereEqualTo("employerBusiness", loggedInManager.employerBusiness)
//                .get()
//                .addOnSuccessListener { document: DocumentSnapshot ->
//                    val item = document.toObject(GameDetails::class.java)
//                    userGameDetails = item?.copy()!!
//                    Log.d(TAG, "getUserGameDetails: ${item.highScore}")
//
//                }
//
//        }
        //return userGameDetails

    }

    private fun setCardClicks() {
        binding.homeCardEasy.setOnClickListener {
            val difficulty = resources.getString(R.string.easy)
            val navToRules = HomeDirections.actionHome2ToRules(difficulty)
            findNavController().navigate(navToRules)
        }
        binding.homeCardMedium.setOnClickListener {
            val difficulty = resources.getString(R.string.medium)
            if (userEasyGameDetails.hasPlayed) {
                val navToRules = HomeDirections.actionHome2ToRules(difficulty)
                findNavController().navigate(navToRules)
            } else {
                requireContext().toast(resources.getString(R.string.play_easy_first))
            }
        }
        binding.homeCardHard.setOnClickListener {
            val difficulty = resources.getString(R.string.hard)
            if (userMediumGameDetails.hasPlayed) {
                val navToRules = HomeDirections.actionHome2ToRules(difficulty)
                findNavController().navigate(navToRules)
            } else {
                requireContext().toast(resources.getString(R.string.play_medium_first))
            }
        }



        binding.homeRandomDifficultyBtn.setOnClickListener {
            var highestListNumber = difficulties.size
            //var randomText: String

            if (!userEasyGameDetails.hasPlayed) {
                val randomText = DIFFICULTY_EASY
                val navToRules = HomeDirections.actionHome2ToRules(randomText)
                findNavController().navigate(navToRules)
            }
            if (!userMediumGameDetails.hasPlayed) {
                highestListNumber = 1
                val randomIndex = (0..highestListNumber).random()
                val randomText = difficulties[randomIndex]
                Log.d(TAG, "setCardClicks: highestListNumber $highestListNumber")
                val navToRules = HomeDirections.actionHome2ToRules(randomText)
                findNavController().navigate(navToRules)
            } else {
                val randomIndex = (0..highestListNumber).random()
                val randomText = difficulties[randomIndex]
                Log.d(TAG, "setCardClicks: highestListNumber $highestListNumber")
                val navToRules = HomeDirections.actionHome2ToRules(randomText)
                findNavController().navigate(navToRules)
            }

            // Use the randomText variable as needed

        }
    }

    private fun getLeaderboard(username: String, difficulty: String) {

        var orderNumberString = ""
        CoroutineScope(Dispatchers.IO).launch {

            Common.leaderBoardCollectionRef.document(difficulty)
                .get()
                .addOnSuccessListener { document: DocumentSnapshot ->
                    val item = document.toObject(Leaderboard::class.java)
                    val mLeaderBoardMap = item?.ranks

                    val orderedLeaderBoardMap =
                        mLeaderBoardMap!!.toSortedMap(compareByDescending { mLeaderBoardMap[it] })

                    if (!orderedLeaderBoardMap.keys.contains(username)) {
                        Log.d(TAG, "getLeaderboard: $difficulty $username not in")
                        orderNumberString = "N/A"
                    } else {
                        for (usernames in orderedLeaderBoardMap.keys) {
                            if (usernames == username) {
                                val orderNumber = orderedLeaderBoardMap.keys.indexOf(username) + 1
                                orderNumberString = orderNumber.toString()
                                Log.d(TAG, "getLeaderboard: $username $difficulty $orderNumber")

                            }
                        }
                    }
                    when (difficulty) {
                        DIFFICULTY_EASY -> {
                            binding.easyLeaderBoard.text = orderNumberString
                        }

                        DIFFICULTY_MEDIUM -> {
                            binding.mediumLeaderBoard.text = orderNumberString
                            Log.d(TAG, "getLeaderboard: $difficulty $orderNumberString")
                        }

                        DIFFICULTY_HARD -> {
                            binding.hardLeaderBoard.text = orderNumberString
                        }
                    }

                }
        }
    }

    private fun getUser() {
        getRealtimeSecurityTips()

        var loggedUser = UserData()
        CoroutineScope(Dispatchers.IO).launch {
            //Common.userCollectionRef.whereEqualTo("userId", Common.auth.uid.toString())
            Common.userCollectionRef.document("ElE9dfN1rXVaJ0MD8IsJv046BnV2")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        requireContext().toast(error.message.toString())
                        return@addSnapshotListener
                    }
                    if (value != null && value.exists()) {
                        loggedUser = value.toObject(UserData::class.java)!!
                        for (difficulty in difficulties)
                            getLeaderboard(loggedUser.username, difficulty)
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
        slideRunnable?.cancel()
        slideTimer?.cancel()
        slideRunnable = null
        slideTimer = null
    }

}