package com.androidstrike.schoolprojects.securitygame.features.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentLeaderBoardDetailBinding
import com.androidstrike.schoolprojects.securitygame.models.Leaderboard
import com.androidstrike.schoolprojects.securitygame.models.LeaderboardDetails
import com.androidstrike.schoolprojects.securitygame.utils.Common.DIFFICULTY_EASY
import com.androidstrike.schoolprojects.securitygame.utils.Common.leaderBoardCollectionRef
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EasyLeaderBoard : Fragment() {

    private lateinit var leaderBoardAdapter: LeaderBoardAdapter


    private var _binding: FragmentLeaderBoardDetailBinding? = null
    private val binding get() = _binding!!

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
            val navToQuizRules = LeaderBoardFragmentDirections.actionLeaderBoardFragmentToRules(
                DIFFICULTY_EASY
            )
            findNavController().navigate(navToQuizRules)
        }

        populateLeaderBoard()

    }

    private fun populateLeaderBoard() {
        CoroutineScope(Dispatchers.IO).launch {
            //userCollectionRef.document(auth.uid.toString()).collection(GAME_DETAILS_REF)
            leaderBoardCollectionRef.document(DIFFICULTY_EASY)
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}