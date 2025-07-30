package com.refactoringlife.lizimportadosadminv2.features.home.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refactoringlife.lizimportadosadminv2.core.network.fireStore.FireStoreStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.util.Date

data class HomeStats(
    val ventasSemanales: List<Pair<Date, Double>> = emptyList(),
    val productosActivos: Int = 0,
    val productosVendidos: Int = 0,
    val combosActivos: Int = 0,
    val combosVendidos: Int = 0,
    val productosBajoStock: List<String> = emptyList()
)

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val stats: HomeStats) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "HomeViewModel"
    }
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // Lista de mensajes motivacionales
    val mensajesMotivacionales = listOf(
        "¡Hoy es un gran día para lograr nuevas ventas!",
        "Recuerda: cada producto vendido es un cliente feliz.",
        "¡Sigue así! Tu esfuerzo se refleja en los resultados.",
        "La constancia es la clave del éxito.",
        "¡Vamos por más! Cada día es una nueva oportunidad.",
        "El éxito es la suma de pequeños esfuerzos repetidos día tras día.",
        "¡Tu dedicación mueve este negocio!",
        "No te detengas, los grandes logros requieren tiempo.",
        "¡Eres el motor de Liz Importados!",
        "Hoy puedes superar tus propias metas.",
        "Siempre recorda que no estas sola.",
        "Hay dias dificiles, pero vos podes con todo!",
        "Un dia nuevo, es una nueva oportunidad de comenzar!, vamos a romperla!!!"
    )
    
    init {
        loadStats()
    }
    
    fun loadStats() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                
                val ventasSemanales = FireStoreStats.getVentasSemanales()
                val (productosActivos, productosVendidos) = FireStoreStats.getMetricasProductos()
                val (combosActivos, combosVendidos) = FireStoreStats.getMetricasCombos()
                val productosBajoStock = FireStoreStats.getProductosBajoStock()
                
                val stats = HomeStats(
                    ventasSemanales = ventasSemanales,
                    productosActivos = productosActivos,
                    productosVendidos = productosVendidos,
                    combosActivos = combosActivos,
                    combosVendidos = combosVendidos,
                    productosBajoStock = productosBajoStock
                )
                
                _uiState.value = HomeUiState.Success(stats)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando estadísticas: ${e.message}")
                _uiState.value = HomeUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun getMensajeMotivacional(): String {
        return mensajesMotivacionales.random()
    }
} 