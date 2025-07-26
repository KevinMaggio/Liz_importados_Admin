package com.refactoringlife.lizimportadosadminv2.core.network.fireStore

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

object FireStoreStats {
    private val db = FirebaseFirestore.getInstance()

    // Obtener ventas de los últimos 7 días (fecha, total)
    suspend fun getVentasSemanales(): List<Pair<Date, Double>> {
        val ventasPorDia = mutableMapOf<String, Double>()
        val calendar = Calendar.getInstance()
        val hoy = calendar.time
        // Calcular fecha de hace 6 días
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val hace7Dias = calendar.time

        val snapshot = db.collection("ventas")
            .whereGreaterThanOrEqualTo("fecha", hace7Dias)
            .get().await()

        for (doc in snapshot.documents) {
            val fecha = doc.getDate("fecha") ?: continue
            val total = doc.getDouble("total") ?: 0.0
            // Agrupar por día (yyyy-MM-dd)
            val key = android.text.format.DateFormat.format("yyyy-MM-dd", fecha).toString()
            ventasPorDia[key] = (ventasPorDia[key] ?: 0.0) + total
        }
        // Generar lista de los últimos 7 días
        val resultado = mutableListOf<Pair<Date, Double>>()
        calendar.time = hoy
        for (i in 0..6) {
            val key = android.text.format.DateFormat.format("yyyy-MM-dd", calendar.time).toString()
            resultado.add(0, calendar.time to (ventasPorDia[key] ?: 0.0))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return resultado
    }

    // Obtener métricas de productos (activos, vendidos)
    suspend fun getMetricasProductos(): Pair<Int, Int> {
        val snapshot = db.collection("products").get().await()
        var activos = 0
        var vendidos = 0
        for (doc in snapshot.documents) {
            if (doc.getBoolean("is_available") == true) activos++
            vendidos += doc.getLong("vendidos")?.toInt() ?: 0
        }
        return activos to vendidos
    }

    // Obtener métricas de combos (activos, vendidos)
    suspend fun getMetricasCombos(): Pair<Int, Int> {
        val snapshot = db.collection("combos").get().await()
        var activos = 0
        var vendidos = 0
        for (doc in snapshot.documents) {
            if (doc.getBoolean("activo") == true) activos++
            vendidos += doc.getLong("vendidos")?.toInt() ?: 0
        }
        return activos to vendidos
    }

    // Obtener productos con bajo stock
    suspend fun getProductosBajoStock(umbral: Int = 5): List<String> {
        val snapshot = db.collection("productos")
            .whereLessThanOrEqualTo("stock", umbral)
            .get().await()
        return snapshot.documents.mapNotNull { it.getString("nombre") }
    }
} 