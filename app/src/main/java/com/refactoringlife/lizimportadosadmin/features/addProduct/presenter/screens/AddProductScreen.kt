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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.ktx.storage
import com.refactoringlife.lizimportadosadmin.core.dto.request.ProductRequest
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
    val (selectedUris, setSelectedUris) = remember { mutableStateOf<List<Uri>>(emptyList()) }
    val (uploadedUrls, setUploadedUrls) = remember { mutableStateOf<List<String>>(emptyList()) }
    val (uploadChecks, setUploadChecks) = remember { mutableStateOf<List<Boolean>>(emptyList()) }
    val (error, setError) = remember { mutableStateOf<String?>(null) }
    val (success, setSuccess) = remember { mutableStateOf<String?>(null) }

    // Campos del formulario
    val (name, setName) = remember { mutableStateOf("") }
    val (description, setDescription) = remember { mutableStateOf("") }
    val (brand, setBrand) = remember { mutableStateOf("") }
    val (category, setCategory) = remember { mutableStateOf("") }
    val (comboId, setComboId) = remember { mutableStateOf("") }
    val (comboPrice, setComboPrice) = remember { mutableStateOf("") }
    val (gender, setGender) = remember { mutableStateOf("") }
    val (isAvailable, setIsAvailable) = remember { mutableStateOf(false) }
    val (isOffer, setIsOffer) = remember { mutableStateOf(false) }
    val (offerPrice, setOfferPrice) = remember { mutableStateOf("") }
    val (price, setPrice) = remember { mutableStateOf("") }
    val (season, setSeason) = remember { mutableStateOf("") }
    val (circleOptionFilter, setCircleOptionFilter) = remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            setProcessing(true)
            setSelectedUris(uris)
            setUploadChecks(List(uris.size) { false })
            setUploadedUrls(emptyList())
            coroutineScope.launch {
                val urls = mutableListOf<String>()
                val checks = mutableListOf<Boolean>()
                for ((index, uri) in uris.withIndex()) {
                    val processor = ImageProcessor()
                    val result = processor.removeWhiteBackground(context, uri)
                    if (result.isSuccess) {
                        val transparentUri = result.getOrNull()
                        val webpResult = convertToWebp(context, transparentUri)
                        if (webpResult.isSuccess) {
                            val uploadResult = uploadToFirebaseStorage(webpResult.getOrNull(), context)
                            if (uploadResult.isSuccess) {
                                urls.add(uploadResult.getOrNull()!!)
                                checks.add(true)
                            } else {
                                setError(uploadResult.exceptionOrNull()?.message)
                                checks.add(false)
                            }
                        } else {
                            setError(webpResult.exceptionOrNull()?.message)
                            checks.add(false)
                        }
                    } else {
                        setError(result.exceptionOrNull()?.message)
                        checks.add(false)
                    }
                    setUploadChecks(checks.toList())
                    setUploadedUrls(urls.toList())
                }
                setProcessing(false)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Button(onClick = { imagePickerLauncher.launch("image/*") }, enabled = !processing) {
                Text("Seleccionar Imágenes")
            }
            if (processing) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
            if (selectedUris.isNotEmpty()) {
                Text("Imágenes seleccionadas:", modifier = Modifier.padding(top = 16.dp))
                selectedUris.forEachIndexed { idx, _ ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                        Checkbox(checked = uploadChecks.getOrNull(idx) == true, onCheckedChange = null, enabled = false)
                        Text("Imagen ${idx + 1}")
                        if (uploadChecks.getOrNull(idx) == true) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Subida", tint = Color(0xFF388E3C), modifier = Modifier.size(20.dp).padding(start = 4.dp))
                        }
                    }
                }
            }
            if (error != null) {
                Text(text = "Error: $error", modifier = Modifier.padding(top = 16.dp), color = Color.Red)
            }
            if (success != null) {
                Text(text = "Producto guardado con éxito: $success", modifier = Modifier.padding(top = 16.dp), color = Color(0xFF388E3C))
            }
            Spacer(modifier = Modifier.size(16.dp))
            // Formulario de producto
            OutlinedTextField(value = name, onValueChange = setName, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = setDescription, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = brand, onValueChange = setBrand, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = category, onValueChange = setCategory, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = comboId, onValueChange = setComboId, label = { Text("Combo IDs (separados por coma)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = comboPrice, onValueChange = setComboPrice, label = { Text("Combo Precio") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = gender, onValueChange = setGender, label = { Text("Género") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = offerPrice, onValueChange = setOfferPrice, label = { Text("Precio Oferta") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = setPrice, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = season, onValueChange = setSeason, label = { Text("Temporada") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = circleOptionFilter, onValueChange = setCircleOptionFilter, label = { Text("Filtro Círculo") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isAvailable, onCheckedChange = setIsAvailable)
                Text("Disponible")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isOffer, onCheckedChange = setIsOffer)
                Text("En Oferta")
            }
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                onClick = {
                    setProcessing(true)
                    coroutineScope.launch {
                        val id = UUID.randomUUID().toString()
                        val product = ProductRequest(
                            id = id,
                            name = name.ifBlank { null },
                            description = description.ifBlank { null },
                            brand = brand.ifBlank { null },
                            category = category.ifBlank { null },
                            comboId = if (comboId.isNotBlank()) comboId.split(",").map { it.trim() } else null,
                            comboPrice = comboPrice.toIntOrNull(),
                            gender = gender.ifBlank { null },
                            images = if (uploadedUrls.isNotEmpty()) uploadedUrls else null,
                            isAvailable = isAvailable,
                            isOffer = isOffer,
                            offerPrice = offerPrice.toIntOrNull() ?: 0,
                            price = price.toIntOrNull(),
                            season = season.ifBlank { null },
                            circleOptionFilter = circleOptionFilter.ifBlank { null }
                        )
                        val result = saveProductToFirestore(product)
                        if (result.isSuccess) {
                            setSuccess(product.id)
                        } else {
                            setError(result.exceptionOrNull()?.message)
                        }
                        setProcessing(false)
                    }
                },
                enabled = !processing && uploadedUrls.isNotEmpty() && uploadedUrls.size == selectedUris.size
            ) {
                Text("Guardar Producto")
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

suspend fun saveProductToFirestore(product: ProductRequest): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        val db = Firebase.firestore
        db.collection("products").document(product.id).set(product).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
} 