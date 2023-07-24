package com.androidstrike.schoolprojects.securitygame.features.home

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import com.androidstrike.schoolprojects.securitygame.utils.Common.auth
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
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.homeCustomToolBar)

        getUser()

        difficulties = resources.getStringArray(R.array.difficulties)
        for (difficulty in difficulties) {
            getUserGameDetails(difficulty)
            Log.d(TAG, "onViewCreated: $difficulty")
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

        val handler = Handler(Looper.getMainLooper())
        slideTimer = Timer()
        slideRunnable = object : TimerTask() {
            override fun run() {
                handler.post{
                    // Increment the current item index or reset to 0
                    val currentItem = binding.securityTipsBanner.currentItem
                    val nextItem =
                        if (currentItem == securityTipsAdapter!!.itemCount - 1) 0 else currentItem + 1

                    binding.securityTipsBanner.setCurrentItem(nextItem, true)
                }

            }
        }

// Schedule the slideRunnable to run every few seconds
        slideTimer?.scheduleAtFixedRate(slideRunnable, 10000, 10000)

    }

    private fun getUserGameDetails(difficulty: String) {//: GameDetails {


        Log.d(TAG, "getUserGameDetails: ")


        var loggedUser = UserData()
        CoroutineScope(Dispatchers.IO).launch {
            //showProgress()
            Common.userCollectionRef.document(Common.auth.uid.toString()).collection(
//            Common.userCollectionRef.document("ElE9dfN1rXVaJ0MD8IsJv046BnV2").collection(
                GAME_DETAILS_REF
            ).document(difficulty)
                .get()
                .addOnSuccessListener { document: DocumentSnapshot ->
                    Log.d(TAG, "getUserGameDetails: $document")
                    Log.d(TAG, "getUserGameDetails: ${document?.data}")
              //      hideProgress()
                //}
//                .addSnapshotListener { value, error ->
//                    hideProgress()
//                    if (error != null) {
//                        requireContext().toast(error.message.toString())
//                        return@addSnapshotListener
//                    }
                    when (difficulty) {
                        DIFFICULTY_EASY -> {
                            Log.d(TAG, "getUserGameDetails: $document")
                            Log.d(TAG, "getUserGameDetails: ${document?.data}")

                            if (document != null && document.exists()) {
                                val userGameDetails = document.toObject(GameDetails::class.java)!!
                                userEasyGameDetails = userGameDetails
                                binding.easyHighScore.text = userEasyGameDetails.highScore.ifEmpty {
                                    resources.getString(R.string.nil)
                                }
                                setCardClicks()
                            }else{
                                binding.easyHighScore.text = resources.getString(R.string.nil)
                                userEasyGameDetails.hasPlayed = false
                                setCardClicks()
                            }
                        }

                        DIFFICULTY_MEDIUM -> {
                            if (document.exists()) {
                                val userGameDetails = document.toObject(GameDetails::class.java)!!
                                userMediumGameDetails = userGameDetails
                                binding.mediumHighScore.text = userMediumGameDetails.highScore.ifEmpty {
                                    resources.getString(R.string.nil)
                                }
                                setCardClicks()
                            }else{
                                binding.mediumHighScore.text = resources.getString(R.string.nil)
                                userMediumGameDetails.hasPlayed = false
                                setCardClicks()
                            }
                        }

                        DIFFICULTY_HARD -> {

                            if (document != null && document.exists()) {
                                val userGameDetails = document?.toObject(GameDetails::class.java)!!
                                userHardGameDetails = userGameDetails
                                binding.hardHighScore.text = userHardGameDetails.highScore.ifEmpty {
                                    resources.getString(R.string.nil)
                                }
                                setCardClicks()
                            }else{
                                binding.hardHighScore.text = resources.getString(R.string.nil)
                                userHardGameDetails.hasPlayed = false
                                setCardClicks()
                            }
                        }
                    }

                }

        }

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

        binding.homeLeaderboardBtn.setOnClickListener {
            val navToLeaderBoard = HomeDirections.actionHome2ToLeaderBoardFragment()
            findNavController().navigate(navToLeaderBoard)
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
                        orderNumberString = resources.getString(R.string.nil)
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
                            Log.d(TAG, "getLeaderboard: $difficulty $orderNumberString")
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
            Common.userCollectionRef.document(Common.auth.uid.toString())
//            Common.userCollectionRef.document("ElE9dfN1rXVaJ0MD8IsJv046BnV2")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        requireContext().toast(error.message.toString())
                        return@addSnapshotListener
                    }
                    if (value != null && value.exists()) {
                        loggedUser = value.toObject(UserData::class.java)!!
                        binding.homeCustomToolBar.title = loggedUser.username
                        for (difficulty in difficulties)
                            getLeaderboard(loggedUser.username, difficulty)
                    }
                }
//
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                val navToStart = HomeDirections.actionHome2ToSignIn()
                findNavController().navigate(navToStart)
            }
        }
        return super.onOptionsItemSelected(item)
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