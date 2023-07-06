package com.androidstrike.schoolprojects.securitygame.features.home

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
import com.androidstrike.schoolprojects.securitygame.utils.Common.securityTipsCollectionRef
import com.androidstrike.schoolprojects.securitygame.utils.enable
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

    private var slideTimer: Timer? = null
    private var slideRunnable: TimerTask? = null

    private var loggedInUser = UserData()

//    private var userEasyGameDetails = GameDetails()
//    private var userMediumGameDetails = GameDetails()
//    private var userHardGameDetails = GameDetails()


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


        getRealtimeSecurityTips()

        val difficulties = resources.getStringArray(R.array.difficulties)

        binding.homeCardEasy.setOnClickListener {
            val difficulty = resources.getString(R.string.easy)
            val navToRules = HomeDirections.actionHome2ToRules(difficulty)
            findNavController().navigate(navToRules)
        }
        binding.homeCardMedium.setOnClickListener {
            val difficulty = resources.getString(R.string.medium)
            val navToRules = HomeDirections.actionHome2ToRules(difficulty)
            findNavController().navigate(navToRules)
        }
        binding.homeCardHard.setOnClickListener {
            val difficulty = resources.getString(R.string.hard)
            val navToRules = HomeDirections.actionHome2ToRules(difficulty)
            findNavController().navigate(navToRules)
        }
        binding.homeRandomDifficultyBtn.setOnClickListener {
            val randomIndex = (0..2).random()
            val randomText = difficulties[randomIndex]

            // Use the randomText variable as needed
            val difficulty = randomText
            val navToRules = HomeDirections.actionHome2ToRules(difficulty)
            findNavController().navigate(navToRules)
        }

    }

    private fun getRealtimeSecurityTips() {

        binding.easyHighScore.text = getUserGameDetails(DIFFICULTY_EASY).highScore
        Log.d(TAG, "getRealtimeSecurityTips: ${getUserGameDetails(DIFFICULTY_EASY)}")
        binding.mediumHighScore.text = getUserGameDetails(DIFFICULTY_MEDIUM).highScore
        binding.hardHighScore.text = getUserGameDetails(DIFFICULTY_HARD).highScore
        binding.easyLeaderBoard.text = getLeaderboard(DIFFICULTY_EASY)
        binding.mediumLeaderBoard.text = getLeaderboard(DIFFICULTY_MEDIUM)
        binding.hardLeaderBoard.text = getLeaderboard(DIFFICULTY_HARD)

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


    private fun getUserGameDetails(difficulty: String): GameDetails {

        var userGameDetails = GameDetails()
        CoroutineScope(Dispatchers.IO).launch {
            //userCollectionRef.document(auth.uid.toString()).collection(GAME_DETAILS_REF)
            Common.userCollectionRef.document("ElE9dfN1rXVaJ0MD8IsJv046BnV2")
                .collection(Common.GAME_DETAILS_REF).document(
                DIFFICULTY_EASY
            )
                //.document(difficulty)// whereEqualTo("employerBusiness", loggedInManager.employerBusiness)
                .get()
                .addOnSuccessListener { document: DocumentSnapshot ->
                    val item = document.toObject(GameDetails::class.java)
                    userGameDetails = item?.copy()!!
                    Log.d(TAG, "getUserGameDetails: ${item.highScore}")

                }

        }
        return userGameDetails

    }

    private fun getLeaderboard(difficulty: String): String {

        var orderNumber = 0
        CoroutineScope(Dispatchers.IO).launch {

            Common.leaderBoardCollectionRef.document(difficulty)
                .get()
                .addOnSuccessListener { document: DocumentSnapshot ->
                    val item = document.toObject(Leaderboard::class.java)
                    val mLeaderBoardMap = item?.ranks

                    val user = loggedInUser.username

                    val orderedLeaderBoardMap =
                        mLeaderBoardMap!!.toSortedMap(compareByDescending { mLeaderBoardMap[it] })

                    orderNumber = orderedLeaderBoardMap.keys.indexOf(user) + 1

                }
        }
        return orderNumber.toString()
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