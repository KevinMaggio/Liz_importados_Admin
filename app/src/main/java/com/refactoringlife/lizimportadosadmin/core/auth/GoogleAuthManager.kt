package com.refactoringlife.lizimportados.core.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.refactoringlife.lizimportados.core.utils.GOOGLE_ID
import com.refactoringlife.lizimportados.core.utils.getGoogleSignInIntent
import kotlinx.coroutines.tasks.await
import android.util.Log

class GoogleAuthManager(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    companion object {
        private const val TAG = "GoogleAuthManager"
    }
    
    /**
     * Obtiene el Intent para iniciar el flujo de autenticación de Google
     */
    fun getSignInIntent(): Intent {
        Log.d(TAG, "🔑 Obteniendo Intent de Google Sign-In")
        return getGoogleSignInIntent(context, GOOGLE_ID)
    }
    
    /**
     * Procesa el resultado de la autenticación de Google
     * @param data Intent resultante de la autenticación
     * @return Resultado de la autenticación
     */
    suspend fun handleSignInResult(data: Intent?): AuthResult {
        Log.d(TAG, "📋 Procesando resultado de Google Sign-In")
        return try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
            Log.d(TAG, "👤 Cuenta obtenida: ${account.email}")
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.e(TAG, "❌ ApiException: ${e.statusCode}")
            AuthResult.Error("Error en la autenticación de Google: ${e.statusCode}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inesperado: ${e.message}")
            AuthResult.Error("Error inesperado: ${e.message}")
        }
    }
    
    /**
     * Autentica con Firebase usando las credenciales de Google
     */
    private suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): AuthResult {
        Log.d(TAG, "🔥 Autenticando con Firebase")
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            
            result.user?.let { user ->
                Log.d(TAG, "✅ Usuario autenticado: ${user.displayName}")
                AuthResult.Success(
                    user = AuthUser(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "",
                        photoUrl = user.photoUrl?.toString() ?: ""
                    )
                )
            } ?: AuthResult.Error("No se pudo obtener información del usuario")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en Firebase Auth: ${e.message}")
            AuthResult.Error("Error en la autenticación con Firebase: ${e.message}")
        }
    }
    
    /**
     * Cierra la sesión actual
     */
    fun signOut() {
        auth.signOut()
        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Obtiene el usuario actual
     */
    fun getCurrentUser(): AuthUser? {
        return auth.currentUser?.let { user ->
            AuthUser(
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName ?: "",
                photoUrl = user.photoUrl?.toString() ?: ""
            )
        }
    }
}

/**
 * Resultado de la autenticación
 */
sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Modelo de usuario autenticado
 */
data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String
) 