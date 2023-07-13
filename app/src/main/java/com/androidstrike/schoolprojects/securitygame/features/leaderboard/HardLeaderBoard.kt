package com.androidstrike.schoolprojects.securitygame.features.leaderboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.securitygame.R
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentLeaderBoardDetailBinding
import com.androidstrike.schoolprojects.securitygame.models.GameDetails
import com.androidstrike.schoolprojects.securitygame.models.Leaderboard
import com.androidstrike.schoolprojects.securitygame.models.LeaderboardDetails
import com.androidstrike.schoolprojects.securitygame.utils.Common
import com.androidstrike.schoolprojects.securitygame.utils.toast
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HardLeaderBoard : Fragment() {

    private var _binding: FragmentLeaderBoardDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var leaderBoardAdapter: LeaderBoardAdapter

    private val TAG = "HardLeaderBoard"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderBoardDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderBoard.layoutManager = layoutManager
        binding.rvLeaderBoard.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                layoutManager.orientation
            )
        )

        binding.btnPlayQuiz.setOnClickListener {
            getUserGameDetails()
        }

        populateLeaderBoard()

    }

    private fun populateLeaderBoard() {
        CoroutineScope(Dispatchers.IO).launch {
            //userCollectionRef.document(auth.uid.toString()).collection(GAME_DETAILS_REF)
            Common.leaderBoardCollectionRef.document(Common.DIFFICULTY_HARD)
                .get()
                .addOnSuccessListener { document: DocumentSnapshot ->
                    val item = document.toObject(Leaderboard::class.java)
                    val mLeaderBoardMap = item?.ranks?.toMutableMap()

                    val leaderboardList = mutableListOf<LeaderboardDetails>()

                    val b =
                        mLeaderBoardMap!!.toSortedMap(compareByDescending { mLeaderBoardMap[it] })

                    for (rank in b) {
                        val orderNumber = b.keys.indexOf(
                            rank.key
                        ) + 1
                        val leaderBoardItem = LeaderboardDetails(
                            rank = orderNumber.toString(),
                            username = rank.key,
                            score = rank.value.toString()
                        )
                        leaderboardList.add(leaderBoardItem)
                    }

                    leaderBoardAdapter = LeaderBoardAdapter(leaderboardList)

                    binding.rvLeaderBoard.adapter = leaderBoardAdapter

                }
        }

    }

    private fun getUserGameDetails() {//: GameDetails {
        CoroutineScope(Dispatchers.IO).launch {
            Common.userCollectionRef.document(Common.auth.uid.toString()).collection(
//            Common.userCollectionRef.document("ElE9dfN1rXVaJ0MD8IsJv046BnV2").collection(
                Common.GAME_DETAILS_REF
            ).document(Common.DIFFICULTY_HARD)
                .addSnapshotListener { value, error ->
                    Log.d(TAG, "getUserGameDetails: $value")
                    Log.d(TAG, "getUserGameDetails: ${value?.exists()}")
                    Log.d(TAG, "getUserGameDetails: $error")
                    if (error != null) {
                        requireContext().toast(error.message.toString())
                        return@addSnapshotListener
                    }
                    if (value != null && value.exists()) {
                        Log.d(TAG, "getUserGameDetails: ${value.data}")
                        val userGameDetails = value.toObject(GameDetails::class.java)!!
                        Log.d(TAG, "getUserGameDetails: $userGameDetails")
                        if (userGameDetails.hasPlayed) {
                            val navToQuizRules =
                                LeaderBoardFragmentDirections.actionLeaderBoardFragmentToRules(
                                    Common.DIFFICULTY_HARD
                                )
                            findNavController().navigate(navToQuizRules)
                        }
                    }
                    else {
                        requireContext().toast(resources.getString(R.string.play_medium_first))
                    }
                }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}