@file:Suppress("DEPRECATION")

package com.androidstrike.schoolprojects.securitygame.features.quiz

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.androidstrike.schoolprojects.securitygame.R
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentQuizBinding
import com.androidstrike.schoolprojects.securitygame.models.Question
import com.androidstrike.schoolprojects.securitygame.utils.Common.QUESTIONS_REF
import com.androidstrike.schoolprojects.securitygame.utils.Common.quizRulesCollectionRef
import com.androidstrike.schoolprojects.securitygame.utils.enable
import com.androidstrike.schoolprojects.securitygame.utils.toast
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val args: QuizFragmentArgs by navArgs()
    private lateinit var difficulty: String

    private var questions: MutableList<Question> = mutableListOf()

    var questionIndex = 1
    private val listIndex = questionIndex - 1
    private var userScore = 0
    private var correctAnswers = 0

    private var countdownTimer: CountDownTimer? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        difficulty = args.difficulty

        getQuestions()
    }

    private fun getQuestions() {
        CoroutineScope(Dispatchers.IO).launch {
            //userCollectionRef.document(auth.uid.toString()).collection(GAME_DETAILS_REF)
            quizRulesCollectionRef.document(difficulty).collection(QUESTIONS_REF)
                //.document(difficulty)// whereEqualTo("employerBusiness", loggedInManager.employerBusiness)
                .get()
                .addOnSuccessListener { snapshot: QuerySnapshot ->
                    for (document in snapshot) {
                        val item = document.toObject(Question::class.java)
                        questions.add(item)
                    }
                    questions.shuffle()

                    populateQuestions()

                }

        }


    }

    private fun populateQuestions() {

        with(binding) {

            btnQuit.setOnClickListener {
                countdownTimer?.cancel()
                showFinishScreen()
            }

            if (questionIndex > questions.size) {
                showFinishScreen()
                requireContext().toast("Over")

            } else {
                resetViews()


                quizQuestionNumber.text = resources.getString(
                    R.string.quiz_question_number,
                    questionIndex.toString(),
                    questions.size.toString()
                )
                questionText.text = questions[listIndex].question
                val options = questions[listIndex].options.shuffled()
                btnOption1.text = options[0]
                btnOption2.text = options[1]
                btnOption3.text = options[2]
                btnOption4.text = options[3]

                quizScore.text = userScore.toString()
                //questionText.text = questions[listIndex].question

                btnHint.setOnClickListener {
                    infoText.apply {
                        text = questions[listIndex].hint
                        setTextColor(resources.getColor(R.color.primary))
                    }
                }

                val allOptionButtons = listOf(btnOption1, btnOption2, btnOption3, btnOption4)

                btnOption1.setOnClickListener {
                    countdownTimer?.cancel()
                    val option = it as Button
                    btnOption1.enable(false)
                    btnOption2.enable(false)
                    btnOption3.enable(false)
                    btnOption4.enable(false)
                    if (checkCorrectAnswer(option.text.toString())) {
                        btnOption1.setBackgroundColor(resources.getColor(R.color.correct))
                        userScore = userScore + 2
                        quizScore.text = userScore.toString()
                        requireContext().toast(resources.getString(R.string.correct))
                        correctAnswers += 1
                        infoText.apply {
                            visibility = View.VISIBLE
                            text = questions[listIndex].info
                            setTextColor(resources.getColor(R.color.correct))
                        }
                        btnPass.apply {
                            infoText.visibility = View.INVISIBLE
                            this.text = resources.getString(R.string.pass_quiz_question)
                            text = resources.getString(R.string.next)
                            setOnClickListener {
                                questionIndex++
                                populateQuestions()
                            }
                        }

                    } else {
                        btnOption1.setBackgroundColor(resources.getColor(R.color.wrong))
                        for (btn in allOptionButtons){
                            if (btn.text == questions[listIndex].answer)
                                btn.setBackgroundColor(resources.getColor(R.color.correct))
                        }
                        infoText.apply {
                            visibility = View.VISIBLE
                            text = questions[listIndex].info
                            setTextColor(resources.getColor(R.color.wrong))
                        }
                        btnPass.apply {
                            text = resources.getString(R.string.next)
                            setOnClickListener {
                                questionIndex++
                                populateQuestions()
                            }
                        }

                    }

                }
                btnOption2.setOnClickListener {
                    countdownTimer?.cancel()
                    btnOption1.enable(false)
                    btnOption2.enable(false)
                    btnOption3.enable(false)
                    btnOption4.enable(false)

                    val option = it as Button
                    if (checkCorrectAnswer(option.text.toString())) {
                        btnOption2.setBackgroundColor(resources.getColor(R.color.correct))
                        userScore += 2
                        quizScore.text = userScore.toString()
                        requireContext().toast(resources.getString(R.string.correct))
                        correctAnswers += 1
                        infoText.apply {
                            visibility = View.VISIBLE
                            text = questions[listIndex].info
                            setTextColor(resources.getColor(R.color.correct))
                        }
                        btnPass.apply {
                            text = resources.getString(R.string.next)
                            setOnClickListener {
                                questionIndex++
                                populateQuestions()
                            }
                        }

                    } else {
                        btnOption2.setBackgroundColor(resources.getColor(R.color.wrong))
                        for (btn in allOptionButtons){
                            if (btn.text == questions[listIndex].answer)
                                btn.setBackgroundColor(resources.getColor(R.color.correct))
                        }
                        infoText.apply {
                            visibility = View.VISIBLE
                            text = questions[listIndex].info
                            setTextColor(resources.getColor(R.color.wrong))
                        }
                        btnPass.apply {
                            text = resources.getString(R.string.next)
                            setOnClickListener {
                                infoText.visibility = View.INVISIBLE
                                this.text = resources.getString(R.string.pass_quiz_question)
                                questionIndex++
                                populateQuestions()
                            }
                        }

                    }
                }
                btnOption3.setOnClickListener {
                    countdownTimer?.cancel()
                    btnOption1.enable(false)
                    btnOption2.enable(false)
                    btnOption3.enable(false)
                    btnOption4.enable(false)

                    val option = it as Button
                    if (checkCorrectAnswer(option.text.toString())) {
                        btnOption3.setBackgroundColor(resources.getColor(R.color.correct))
                        userScore += 2
                        quizScore.text = userScore.toString()
                        requireContext().toast(resources.getString(R.string.correct))
                        correctAnswers += 1
                        infoText.apply {
                            visibility = View.VISIBLE
                            text = questions[listIndex].info
                            setTextColor(resources.getColor(R.color.correct))
                        }
                        btnPass.apply {
                            text = resources.getString(R.string.next)
                            setOnClickListener {
                                infoText.visibility = View.INVISIBLE
                                this.text = resources.getString(R.string.pass_quiz_question)
                                questionIndex++
                                populateQuestions()
                            }
                        }

                    } else {
                        btnOption3.setBackgroundColor(resources.getColor(R.color.wrong))
                        for (btn in allOptionButtons){
                            if (btn.text == questions[listIndex].answer)
                                btn.setBackgroundColor(resources.getColor(R.color.correct))
                        }
                        infoText.apply {
                            visibility = View.VISIBLE
                            text = questions[listIndex].info
                            setTextColor(resources.getColor(R.color.wrong))
                        }
                        btnPass.apply {
                            text = resources.getString(R.string.next)
                            setOnClickListener {
                                questionIndex++
                                populateQuestions()
                            }
                        }

                    }
                }
                btnOption4.setOnClickListener {
                    countdownTimer?.cancel()
                    btnOption1.enable(false)
                    btnOption2.enable(false)
                    btnOption3.enable(false)
                    btnOption4.enable(false)

                    val option = it as Button
                    if (checkCorrectAnswer(option.text.toString())) {
                        btnOption4.setBackgroundColor(resources.getColor(R.color.correct))
                        userScore += 2
                        quizScore.text = userScore.toString()
                        requireContext().toast(resources.getString(R.string.correct))
                        correctAnswers += 1
                        infoText.apply {
                            visibility = View.VISIBLE
                            text = questions[listIndex].info
                            setTextColor(resources.getColor(R.color.correct))
                        }
                        btnPass.apply {
                            text = resources.getString(R.string.next)
                            setOnClickListener {
                                questionIndex++
                                infoText.visibility = View.INVISIBLE
                                this.text = resources.getString(R.string.pass_quiz_question)
                                populateQuestions()
                            }
                        }

                    } else {
                        btnOption4.setBackgroundColor(resources.getColor(R.color.wrong))
                        for (btn in allOptionButtons){
                            if (btn.text == questions[listIndex].answer)
                                btn.setBackgroundColor(resources.getColor(R.color.correct))
                        }
                        infoText.apply {
                            visibility = View.VISIBLE
                            text = questions[listIndex].info
                            setTextColor(resources.getColor(R.color.wrong))
                        }
                        btnPass.apply {
                            text = resources.getString(R.string.next)
                            setOnClickListener {
                                questionIndex++
                                populateQuestions()
                            }
                        }

                    }
                }


                btnPass.setOnClickListener {
                    countdownTimer?.cancel()
                    questionIndex++
                    populateQuestions()
                }
            }


        }


    }

    private fun resetViews() {

        with(binding){
            val questionTime =
                when(difficulty){
                    "Easy" ->{
                        45000L
                    }
                    "Medium" ->{
                        30000L
                    }
                    "Hard" ->{
                        15000L
                    }else ->{
                    45000L
                }
                }


            btnOption1.setBackgroundColor(resources.getColor(R.color.primary))
            btnOption2.setBackgroundColor(resources.getColor(R.color.primary))
            btnOption3.setBackgroundColor(resources.getColor(R.color.primary))
            btnOption4.setBackgroundColor(resources.getColor(R.color.primary))

            infoText.visibility = View.INVISIBLE
            btnPass.text = resources.getString(R.string.pass_quiz_question)
            btnOption1.enable(true)
            btnOption2.enable(true)
            btnOption3.enable(true)
            btnOption4.enable(true)
            startCountdown(questionTime)
        }

    }

    private fun showFinishScreen() {

        val navToFinishScreen = QuizFragmentDirections.actionQuizFragmentToQuizDone(
            totalQuestions = questions.size.toString(),
            answeredQuestions = (questionIndex-1).toString(),
            score = userScore.toString(),
            difficulty = difficulty,
            correctAnswers = correctAnswers.toString()
        )
        findNavController().navigate(navToFinishScreen)

    }

    private fun checkCorrectAnswer(text: String): Boolean {

        return text == questions[listIndex].answer
    }

    private fun startCountdown(questionTime: Long) {
        countdownTimer = object : CountDownTimer(questionTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.txtTimer.text = resources.getString(R.string.seconds_count_down, seconds.toString())
                if (seconds <= 10){
                    binding.txtTimer.setTextColor(resources.getColor(R.color.wrong))
                }
            }

            override fun onFinish() {
                binding.txtTimer.text = resources.getString(R.string.question_time_up)
                questionIndex++
                populateQuestions()
            }
        }

        countdownTimer?.start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}