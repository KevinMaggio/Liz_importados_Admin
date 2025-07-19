package com.refactoringlife.lizimportadosadmin.features.home.presenter.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.refactoringlife.lizimportadosadmin.ui.theme.ColorWhiteLipsy

@Composable
fun HomeDataView(
    modifier: Modifier = Modifier,
    onNavigateToAddProduct: () -> Unit = {},
    onNavigateToEditProduct: () -> Unit = {},
    onNavigateToCreateCombo: () -> Unit = {},
    onNavigateToDeleteProduct: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWhiteLipsy)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Panel de Administración",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onNavigateToAddProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 56.dp, width = 200.dp)
            ) {
                Text("Agregar Producto")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToEditProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 56.dp, width = 200.dp)
            ) {
                Text("Editar Producto")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToCreateCombo,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 56.dp, width = 200.dp)
            ) {
                Text("Crear Combo de Productos")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToDeleteProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 56.dp, width = 200.dp)
            ) {
                Text("Eliminar Producto")
            }
        }
    }
}