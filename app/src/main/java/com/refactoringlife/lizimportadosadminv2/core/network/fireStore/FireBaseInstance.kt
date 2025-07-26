package com.refactoringlife.lizimportadosadminv2.core.network.fireStore

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseProvider {
    val instance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}