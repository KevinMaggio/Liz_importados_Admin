package com.refactoringlife.lizimportadosadminv2.features.combo.presenter.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Modelo específico para productos en combos
data class ComboProduct(
    val id: String,
    val name: String?, // Cambiar de title a name
    val price: Int?,
    val images: List<String>?
)

class ComboViewModel : ViewModel() {
    private val _product1 = MutableStateFlow<ComboProduct?>(null)
    val product1: StateFlow<ComboProduct?> = _product1.asStateFlow()
    
    private val _product2 = MutableStateFlow<ComboProduct?>(null)
    val product2: StateFlow<ComboProduct?> = _product2.asStateFlow()
    
    fun setProduct1(product: ComboProduct) {
        _product1.value = product
    }
    
    fun setProduct2(product: ComboProduct) {
        _product2.value = product
    }
    
    fun clearProduct1() {
        _product1.value = null
    }
    
    fun clearProduct2() {
        _product2.value = null
    }
    
    fun clearAll() {
        _product1.value = null
        _product2.value = null
    }
} 