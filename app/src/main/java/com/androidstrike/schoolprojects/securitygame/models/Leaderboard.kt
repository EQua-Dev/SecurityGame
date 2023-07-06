package com.androidstrike.schoolprojects.securitygame.models

data class Leaderboard(
    var ranks: Map<String, Int> = mapOf()
)
