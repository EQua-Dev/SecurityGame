package com.androidstrike.schoolprojects.securitygame.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object Common {


    const val USER_REF = "Security Game Users"
    const val GAME_DETAILS_REF = "GameDetails"
    const val QUIZ_REF = "Quiz"
    const val LEADER_BOARD_REF = "Leader Board"
    const val QUESTIONS_REF = "Questions"
    const val FULL_DATE_FORMAT = "EEEE, dd MMM, yyyy"
    const val GAME_DETAILS_HIGH_SCORE = "highScore"
    const val GAME_DETAILS_LAST_PLAYED = "lastPlayed"
    const val GAME_DETAILS_LAST_SCORE = "lastScore"
    const val GAME_DETAILS_LAST_LOCATION_PLAYED = "lastLocationPlayed"
    const val GAME_DETAILS_LAST_LEADER_BOARD_RANK = "leaderBoardRank"
    const val GAME_DETAILS_HAS_PLAYED = "hasPlayed"
    const val DIFFICULTY_EASY = "Easy"
    const val DIFFICULTY_MEDIUM = "Medium"
    const val DIFFICULTY_HARD = "Hard"
    const val LEADER_BOARD_RANKS = "ranks"

    val auth = FirebaseAuth.getInstance()
    val userCollectionRef = Firebase.firestore.collection(USER_REF)
    val securityTipsCollectionRef = Firebase.firestore.collection("Security Tips")
    val quizRulesCollectionRef = Firebase.firestore.collection("Quiz")
    val leaderBoardCollectionRef = Firebase.firestore.collection(LEADER_BOARD_REF)


}