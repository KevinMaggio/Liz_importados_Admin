package com.refactoringlife.lizimportadosadmin.features.addProduct.presenter.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.refactoringlife.lizimportadosadmin.core.utils.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AddProductScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val (processing, setProcessing) = remember { mutableStateOf(false) }
    val (imageUri, setImageUri) = remember { mutableStateOf<Uri?>(null) }
    val (webpUri, setWebpUri) = remember { mutableStateOf<Uri?>(null) }
    val (downloadUrl, setDownloadUrl) = remember { mutableStateOf<String?>(null) }
    val (error, setError) = remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            setProcessing(true)
            coroutineScope.launch {
                // 1. Remover fondo blanco
                val processor = ImageProcessor()
                val result = processor.removeWhiteBackground(context, uri)
                if (result.isSuccess) {
                    val transparentUri = result.getOrNull()
                    // 2. Convertir a WebP
                    val webpResult = convertToWebp(context, transparentUri)
                    if (webpResult.isSuccess) {
                        setWebpUri(webpResult.getOrNull())
                        // 3. Subir a Firebase Storage
                        val uploadResult = uploadToFirebaseStorage(webpResult.getOrNull(), context)
                        if (uploadResult.isSuccess) {
                            setDownloadUrl(uploadResult.getOrNull())
                            setImageUri(uri)
                        } else {
                            setError(uploadResult.exceptionOrNull()?.message)
                        }
                    } else {
                        setError(webpResult.exceptionOrNull()?.message)
                    }
                } else {
                    setError(result.exceptionOrNull()?.message)
                }
                setProcessing(false)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { imagePickerLauncher.launch("image/*") }, enabled = !processing) {
                Text("Seleccionar Imagen")
            }
            if (processing) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
            if (downloadUrl != null) {
                Text(text = "URL de la imagen:", modifier = Modifier.padding(top = 24.dp))
                Text(text = downloadUrl, modifier = Modifier.padding(8.dp))
                AsyncImage(
                    model = downloadUrl,
                    contentDescription = "Imagen subida",
                    modifier = Modifier.size(200.dp).padding(top = 16.dp)
                )
            }
            if (error != null) {
                Text(text = "Error: $error", modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
suspend fun convertToWebp(context: android.content.Context, uri: Uri?): Result<Uri> = withContext(Dispatchers.IO) {
    try {
        if (uri == null) return@withContext Result.failure(Exception("Uri nula"))
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        if (bitmap == null) return@withContext Result.failure(Exception("No se pudo decodificar la imagen"))
        val fileName = "webp_${UUID.randomUUID()}.webp"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 90, outputStream)
        outputStream.close()
        bitmap.recycle()
        Result.success(Uri.fromFile(file))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun uploadToFirebaseStorage(uri: Uri?, context: android.content.Context): Result<String> = withContext(Dispatchers.IO) {
    try {
        if (uri == null) return@withContext Result.failure(Exception("Uri nula"))
        val storage = Firebase.storage
        val storageRef = storage.reference.child("productos/${UUID.randomUUID()}.webp")
        val stream =
            context.contentResolver.openInputStream(uri) ?: return@withContext Result.failure(
                Exception("No se pudo abrir el archivo")
            )
        val uploadTask = storageRef.putStream(stream)
        val taskSnapshot = uploadTask.await()
        val url = storageRef.downloadUrl.await().toString()
        stream.close()
        Result.success(url)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
} 