package com.androidstrike.schoolprojects.securitygame.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserData(
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val userId: String = ""
): Parcelable
