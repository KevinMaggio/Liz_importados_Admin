package com.refactoringlife.lizimportados.core.network.fireStore

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseProvider {
    val instance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}