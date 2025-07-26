package com.refactoringlife.lizimportadosadminv2.features.addProduct.presenter.screens


import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.refactoringlife.lizimportadosadminv2.core.dto.request.ProductRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.refactoringlife.lizimportadosadminv2.core.utils.ImageProcessor
import com.refactoringlife.lizimportadosadminv2.core.utils.uploadImageToStorage
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.refactoringlife.lizimportadosadminv2.core.utils.ProductConstants
import com.refactoringlife.lizimportadosadminv2.core.composablesLipsy.LipsyDropdown
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

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

    // Agregar estado para tamaños de imágenes
    val imageSizes = remember { mutableStateMapOf<Uri, Long>() }

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
                    val result = processor.processImage(context, uri)
                    if (result.isSuccess) {
                        val optimizedResult = result.getOrNull()!!
                        imageSizes[uri] = optimizedResult.sizeKB // Guardar tamaño
                        val imageUrl = uploadImageToStorage(context, optimizedResult.uri)
                        if (imageUrl != null) {
                            urls.add(imageUrl)
                            checks.add(true)
                        } else {
                            checks.add(false)
                        }
                    } else {
                        checks.add(false)
                    }
                    setUploadChecks(checks.toList())
                    setUploadedUrls(urls.toList())
                }
                setProcessing(false)
            }
        }
    }

    var selectedCategory by remember { mutableStateOf(ProductConstants.SELECT_OPTION) }
    var selectedGender by remember { mutableStateOf(ProductConstants.SELECT_OPTION) }

    // Función para crear el ProductRequest
    fun createProductRequest(): ProductRequest {
        return ProductRequest(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            brand = brand,
            category = ProductConstants.getValueOrEmpty(selectedCategory),
            gender = ProductConstants.getValueOrEmpty(selectedGender),
            price = price.toIntOrNull(),
            images = uploadedUrls
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
    ) {
        SnackbarHost(hostState = snackbarHostState)
        LazyColumn {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                // Descripción - Múltiples líneas
                OutlinedTextField(
                    value = description,
                    onValueChange = setDescription,
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(120.dp), // Altura fija para descripción
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                // Marca - Una línea
                OutlinedTextField(
                    value = brand,
                    onValueChange = setBrand,
                    label = { Text("Marca") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                // Dropdowns
                LipsyDropdown(
                    label = "Categoría",
                    options = ProductConstants.CATEGORIES,
                    selectedOption = selectedCategory,
                    onOptionSelected = { selectedCategory = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                LipsyDropdown(
                    label = "Género",
                    options = ProductConstants.GENDERS,
                    selectedOption = selectedGender,
                    onOptionSelected = { selectedGender = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Precio - Solo números enteros
                OutlinedTextField(
                    value = price,
                    onValueChange = { newValue ->
                        // Solo permitir números
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            setPrice(newValue)
                        }
                    },
                    label = { Text("Precio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

                // Eliminados del formulario: comboId, comboPrice, isOffer, offerPrice, season, circleOptionFilter
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = {
                        setProcessing(true)
                        focusManager.clearFocus()
                        coroutineScope.launch {
                            val product = createProductRequest()
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

suspend fun saveProductToFirestore(product: ProductRequest): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        val db = Firebase.firestore
        // Convertir el producto a Map para asegurar que los campos se guarden correctamente
        val productMap = mapOf(
            "id" to product.id,
            "name" to product.name,
            "description" to product.description,
            "brand" to product.brand,
            "category" to product.category,
            "combo_id" to (product.comboId ?: emptyList()),
            "combo_price" to (product.comboPrice ?: 0),
            "gender" to product.gender,
            "images" to product.images,
            "is_available" to true, // Forzar a true explícitamente
            "is_offer" to false,
            "offer_price" to 0,
            "price" to product.price,
            "season" to "",
            "circle_option_filter" to ""
        )
        db.collection("products").document(product.id).set(productMap).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
} 