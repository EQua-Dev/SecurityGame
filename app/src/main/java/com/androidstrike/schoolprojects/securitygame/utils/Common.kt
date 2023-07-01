package com.androidstrike.schoolprojects.securitygame.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object Common {

    val auth = FirebaseAuth.getInstance()
    val userCollectionRef = Firebase.firestore.collection("Security Game Users")
    val securityTipsCollectionRef = Firebase.firestore.collection("Security Tips")
}