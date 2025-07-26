package com.refactoringlife.lizimportadosadminv2.core.utils

import android.content.Context
import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

suspend fun uploadImageToStorage(context: Context, imageUri: Uri): String? {
    return try {
        val storage = Firebase.storage
        val fileName = "productos/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)
        
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            val uploadTask = storageRef.putStream(inputStream)
            uploadTask.await()
            storageRef.downloadUrl.await().toString()
        }
    } catch (e: Exception) {
        null
    }
} 