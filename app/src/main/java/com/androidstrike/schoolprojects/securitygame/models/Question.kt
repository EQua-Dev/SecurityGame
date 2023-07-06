package com.androidstrike.schoolprojects.securitygame.models

data class Question(
    var question: String = "",
    var hint: String = "",
    var answer: String = "",
    var info: String = "",
    var options: List<String> = listOf()
)
