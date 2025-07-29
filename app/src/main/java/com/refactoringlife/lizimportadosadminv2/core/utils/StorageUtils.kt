package com.refactoringlife.lizimportadosadminv2.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

suspend fun uploadImageToStorage(context: Context, imageUri: Uri): String? {
    return try {
        Log.d("StorageUtils", "🚀 Iniciando subida de imagen: $imageUri")
        
        val storage = Firebase.storage
        val fileName = "productos/${UUID.randomUUID()}.webp"
        val storageRef = storage.reference.child(fileName)
        
        Log.d("StorageUtils", "📁 Archivo destino: $fileName")
        Log.d("StorageUtils", "🔗 Referencia de storage: $storageRef")
        
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            Log.d("StorageUtils", "📤 Subiendo imagen...")
            Log.d("StorageUtils", "📊 Tamaño del input stream: ${inputStream.available()} bytes")
            
            val uploadTask = storageRef.putStream(inputStream)
            Log.d("StorageUtils", "⏳ Esperando resultado de subida...")
            
            val uploadResult = uploadTask.await()
            Log.d("StorageUtils", "✅ Imagen subida exitosamente")
            Log.d("StorageUtils", "📊 Bytes transferidos: ${uploadResult.bytesTransferred}")
            
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d("StorageUtils", "🔗 URL de descarga: $downloadUrl")
            downloadUrl
        } ?: run {
            Log.e("StorageUtils", "❌ No se pudo abrir el input stream")
            null
        }
    } catch (e: Exception) {
        Log.e("StorageUtils", "❌ Error subiendo imagen: ${e.message}")
        Log.e("StorageUtils", "❌ Tipo de error: ${e.javaClass.simpleName}")
        e.printStackTrace()
        null
    }
} 