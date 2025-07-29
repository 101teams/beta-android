package com.betamotor.app.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.betamotor.app.data.api.auth.AuthRequest
import com.betamotor.app.data.api.forgot_password.ForgotPasswordRequest
import com.betamotor.app.data.api.register.RegisterRequest
import com.betamotor.app.service.AuthService
import com.betamotor.app.utils.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val prefManager: PrefManager
): ViewModel(), DefaultLifecycleObserver {
    private val _loading = mutableStateOf(false)
    val loading = _loading

    suspend fun login(request: AuthRequest): String? {
        _loading.value = true

        val result = authService.login(request)
        val data = result.first
        val text = result.second // this can be a bearer token or an error message

        if (data?.token != null) {
            prefManager.setToken(data.token)
        }

        prefManager.setUser(data)

        _loading.value = false
        return if (data?.token != null) null else text
    }

    suspend fun register(request: RegisterRequest): String? {
        _loading.value = true

        val result = authService.register(request)
        val data = result.first
        val text = result.second // this can be a bearer token or an error message

        _loading.value = false
        return if (data?.user?.id != null) null else text
    }

    suspend fun forgotPassword(request: ForgotPasswordRequest, language: String): String? {
        _loading.value = true

        val result = authService.forgotPassword(request, language)
        val data = result.first
        val text = result.second // this can be a bearer token or an error message

        _loading.value = false
        return if (data?.status == "success") null else text
    }

    fun isLoggedIn(): Boolean {
        return prefManager.getToken().isNotBlank()
    }

    fun logout() {
//        deviceService.resetToken()
        prefManager.setToken("")
        prefManager.setUser(null)
    }

    fun resetToken() {
        authService.resetToken()
    }

    override fun onStop(owner: LifecycleOwner) {
        /**
         * TODO: when using remember me, use pref manager.
         * but, if not using remember me, use memory-scope variable or state so the variable
         * will be reset when the app is closed.
         * the current logic of using onStop/onDestroy is not reliable enough as a solution.
          */
        super.onStop(owner)
    }
}
