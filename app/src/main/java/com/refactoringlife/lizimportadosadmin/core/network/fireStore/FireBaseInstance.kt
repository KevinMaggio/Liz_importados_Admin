package com.refactoringlife.lizimportadosadmin.core.network.fireStore

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseProvider {
    val instance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}