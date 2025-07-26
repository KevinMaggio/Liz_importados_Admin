package com.refactoringlife.lizimportadosadminv2.features.login.presenter.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refactoringlife.lizimportados.core.auth.AuthResult
import com.refactoringlife.lizimportados.core.auth.GoogleAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class LoginViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "LoginViewModel"
    }
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    private var authManager: GoogleAuthManager? = null
    
    /**
     * Inicializa el manager de autenticación
     */
    fun initializeAuthManager(context: Context) {
        Log.d(TAG, "🔧 Inicializando AuthManager")
        authManager = GoogleAuthManager(context)
    }
    
    /**
     * Inicia el proceso de autenticación con Google
     */
    fun signInWithGoogle(context: Context): Intent? {
        return try {
            Log.d(TAG, "🚀 Iniciando autenticación con Google")
            _uiState.value = LoginUiState.Loading
            authManager?.getSignInIntent()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al iniciar autenticación: ${e.message}")
            _uiState.value = LoginUiState.Error("Error al iniciar la autenticación: ${e.message}")
            null
        }
    }
    
    /**
     * Procesa el resultado de la autenticación
     */
    fun handleSignInResult(data: Intent?) {
        Log.d(TAG, "📥 Procesando resultado de autenticación")
        viewModelScope.launch {
            try {
                _uiState.value = LoginUiState.Loading
                
                val result = authManager?.handleSignInResult(data)
                
                when (result) {
                    is AuthResult.Success -> {
                        Log.d(TAG, "✅ Autenticación exitosa: ${result.user.displayName}")
                        _uiState.value = LoginUiState.Success(result.user)
                    }
                    is AuthResult.Error -> {
                        Log.e(TAG, "❌ Error en autenticación: ${result.message}")
                        _uiState.value = LoginUiState.Error(result.message)
                    }
                    null -> {
                        Log.e(TAG, "❌ AuthManager no inicializado")
                        _uiState.value = LoginUiState.Error("Error: AuthManager no inicializado")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inesperado: ${e.message}")
                _uiState.value = LoginUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }
    
    /**
     * Resetea el estado de la UI
     */
    fun resetState() {
        Log.d(TAG, "🔄 Reseteando estado")
        _uiState.value = LoginUiState.Idle
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    fun isUserSignedIn(): Boolean {
        return authManager?.isUserSignedIn() ?: false
    }
    
    /**
     * Cierra la sesión actual
     */
    fun signOut() {
        authManager?.signOut()
        _uiState.value = LoginUiState.Idle
    }
}

/**
 * Estados de la UI para el login
 */
sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: com.refactoringlife.lizimportados.core.auth.AuthUser) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
} 