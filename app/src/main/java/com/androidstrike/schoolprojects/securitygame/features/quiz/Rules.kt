package com.androidstrike.schoolprojects.securitygame.features.quiz

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.securitygame.R
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentRulesBinding
import com.androidstrike.schoolprojects.securitygame.features.home.HomeBannerAdapter
import com.androidstrike.schoolprojects.securitygame.models.GameDetails
import com.androidstrike.schoolprojects.securitygame.models.Quiz
import com.androidstrike.schoolprojects.securitygame.models.SecurityTips
import com.androidstrike.schoolprojects.securitygame.utils.Common.FULL_DATE_FORMAT
import com.androidstrike.schoolprojects.securitygame.utils.Common.GAME_DETAILS_REF
import com.androidstrike.schoolprojects.securitygame.utils.Common.auth
import com.androidstrike.schoolprojects.securitygame.utils.Common.quizRulesCollectionRef
import com.androidstrike.schoolprojects.securitygame.utils.Common.userCollectionRef
import com.androidstrike.schoolprojects.securitygame.utils.getDate
import com.androidstrike.schoolprojects.securitygame.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Rules : Fragment() {

    private var _binding: FragmentRulesBinding? = null
    private val binding get() = _binding!!

    private val args: RulesArgs by navArgs()
    private lateinit var difficulty: String

    lateinit var userGameDetails: GameDetails

    private lateinit var gameRulesAdapter: QuizRulesAdapter

    val TAG = "Rules"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRulesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        difficulty = args.difficulty
        Log.d(TAG, "onViewCreated: $difficulty")

        userGameDetails = GameDetails()

        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvRules.layoutManager = layoutManager
        binding.rvRules.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                layoutManager.orientation
            )
        )

        with(binding) {
            txtQuizCategoryTitle.text =
                resources.getString(R.string.quiz_category_title, difficulty)

            startQuizBtn.setOnClickListener {
                val navToQuiz = RulesDirections.actionRulesToQuizFragment(difficulty)
                findNavController().navigate(navToQuiz)
            }

        }

        getUserGameDetails()
    }

    private fun getUserGameDetails() {

        CoroutineScope(Dispatchers.IO).launch {
            userCollectionRef.document(auth.uid.toString()).collection(GAME_DETAILS_REF)
//            Log.d(TAG, "getUserGameDetails: $difficulty")
//            userCollectionRef.document("ElE9dfN1rXVaJ0MD8IsJv046BnV2").collection(GAME_DETAILS_REF)
                .document(difficulty)
                //.document(difficulty)// whereEqualTo("employerBusiness", loggedInManager.employerBusiness)
                .get()
                .addOnSuccessListener { document: DocumentSnapshot ->
                    val item = document.toObject(GameDetails::class.java)
                    if (item != null) {
                        userGameDetails = item
                    }

                    populateGameDetails()

                }

        }

    }

    private fun populateGameDetails() {

        with(binding) {
            lastPlayedScore.text = if (userGameDetails.lastScore.isEmpty()) {
                resources.getString(R.string.nil)
            } else {
                resources.getString(R.string.game_score, userGameDetails.lastScore)
            }
            highScore.text = if (userGameDetails.highScore.isEmpty()) {
                resources.getString(R.string.nil)
            } else {
                resources.getString(R.string.game_score, userGameDetails.highScore)
            }
            lastPlayedDate.text = if (userGameDetails.lastPlayed.isEmpty()) {
                resources.getString(R.string.nil)
            } else {
                getDate(userGameDetails.lastPlayed.toLong(), FULL_DATE_FORMAT)
            }
            lastLocation.text = userGameDetails.lastLocationPlayed.ifEmpty {
                resources.getString(R.string.nil)
            }
        }

        populateGameRules()

    }

    private fun populateGameRules() {

        CoroutineScope(Dispatchers.IO).launch {
            //userCollectionRef.document(auth.uid.toString()).collection(GAME_DETAILS_REF)
            quizRulesCollectionRef.document(difficulty)
                .get()
                .addOnSuccessListener { document: DocumentSnapshot ->
                    val item = document.toObject(Quiz::class.java)
                    gameRulesAdapter = QuizRulesAdapter(item!!.rules)

                    Log.d(TAG, "populateGameRules: ${item.rules}")

                    binding.rvRules.adapter = gameRulesAdapter

                }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}