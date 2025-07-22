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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val (processing, setProcessing) = remember { mutableStateOf(false) }
    val (selectedUris, setSelectedUris) = remember { mutableStateOf<List<Uri>>(emptyList()) }
    val (uploadedUrls, setUploadedUrls) = remember { mutableStateOf<List<String>>(emptyList()) }
    val (uploadChecks, setUploadChecks) = remember { mutableStateOf<List<Boolean>>(emptyList()) }
    val (error, setError) = remember { mutableStateOf<String?>(null) }
    val (success, setSuccess) = remember { mutableStateOf<String?>(null) }
    val (showSuccess, setShowSuccess) = remember { mutableStateOf(false) }

    // Campos del formulario
    val (name, setName) = remember { mutableStateOf("") }
    val (description, setDescription) = remember { mutableStateOf("") }
    val (brand, setBrand) = remember { mutableStateOf("") }
    val (category, setCategory) = remember { mutableStateOf("") }
    val (gender, setGender) = remember { mutableStateOf("") }
    val (price, setPrice) = remember { mutableStateOf("") }
    // Eliminados del formulario: comboId, comboPrice, isOffer, offerPrice, season, circleOptionFilter

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
        modifier = Modifier.fillMaxSize().imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Text(
                    text = "Agregar Producto",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                )
                Button(onClick = { imagePickerLauncher.launch("image/*") }, enabled = !processing) {
                    Text("Seleccionar Imágenes")
                }
                if (processing) {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                }
                if (selectedUris.isNotEmpty()) {
                    Text("Imágenes seleccionadas:", modifier = Modifier.padding(top = 16.dp))
                }
            }
            itemsIndexed(selectedUris) { idx, _ ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                    Checkbox(checked = uploadChecks.getOrNull(idx) == true, onCheckedChange = null, enabled = false)
                    Text("Imagen ${idx + 1}")
                    if (uploadChecks.getOrNull(idx) == true) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Subida", tint = Color(0xFF388E3C), modifier = Modifier.size(20.dp).padding(start = 4.dp))
                    }
                }
            }
            item {
                if (error != null) {
                    Text(text = "Error: $error", modifier = Modifier.padding(top = 16.dp), color = Color.Red)
                }
                Spacer(modifier = Modifier.size(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = setName,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nombre", color = Color.Gray) }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = setDescription,
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Descripción", color = Color.Gray) }
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = setBrand,
                    label = { Text("Marca") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Marca", color = Color.Gray) }
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = setCategory,
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Categoría", color = Color.Gray) }
                )
                OutlinedTextField(
                    value = gender,
                    onValueChange = setGender,
                    label = { Text("Género") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Género", color = Color.Gray) }
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = setPrice,
                    label = { Text("Precio") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Precio", color = Color.Gray) }
                )
                // Eliminados del formulario: comboId, comboPrice, isOffer, offerPrice, season, circleOptionFilter
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = {
                        setProcessing(true)
                        focusManager.clearFocus()
                        coroutineScope.launch {
                            val id = UUID.randomUUID().toString()
                            val product = ProductRequest(
                                id = id,
                                name = name.ifBlank { null },
                                description = description.ifBlank { null },
                                brand = brand.ifBlank { null },
                                category = category.ifBlank { null },
                                comboId = emptyList(), // Eliminado, enviar vacío
                                comboPrice = 0, // Eliminado, enviar 0
                                gender = gender.ifBlank { null },
                                images = if (uploadedUrls.isNotEmpty()) uploadedUrls else null,
                                isAvailable = true, // Siempre true
                                isOffer = false, // Eliminado, enviar false
                                offerPrice = 0, // Eliminado, enviar 0
                                price = price.toIntOrNull(),
                                season = "", // Eliminado, enviar vacío
                                circleOptionFilter = "" // Eliminado, enviar vacío
                            )
                            val result = saveProductToFirestore(product)
                            if (result.isSuccess) {
                                setSuccess(product.id)
                                setShowSuccess(true)
                                snackbarHostState.showSnackbar("Producto guardado con éxito")
                                // Limpiar formulario
                                setName("")
                                setDescription("")
                                setBrand("")
                                setCategory("")
                                setGender("")
                                setPrice("")
                                setSelectedUris(emptyList())
                                setUploadedUrls(emptyList())
                                setUploadChecks(emptyList())
                            } else {
                                setError(result.exceptionOrNull()?.message)
                            }
                            setProcessing(false)
                        }
                    },
                    enabled = !processing && uploadedUrls.isNotEmpty() && uploadedUrls.size == selectedUris.size
                ) {
                    Text("Finalizar carga")
                }
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