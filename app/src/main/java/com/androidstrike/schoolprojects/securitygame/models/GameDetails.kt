package com.androidstrike.schoolprojects.securitygame.models

data class GameDetails(
    var lastPlayed: String = "",
    var lastScore: String = "",
    var highScore: String = "0",
    var leaderBoardRank: String = "",
    var lastLocationPlayed: String = "",
    var hasPlayed: Boolean = false
    )
