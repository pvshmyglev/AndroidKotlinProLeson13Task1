package ru.netology.nmedia.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.dto.AuthState

class AppAuth(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"
    private val _data = MutableStateFlow(AuthState())
    val data: StateFlow<AuthState>
        get() = _data.asStateFlow()

    init {
        val token = prefs.getString(tokenKey, null)
        val id = prefs.getLong(idKey, 0L)

        if (token != null && prefs.contains(token)) {
            _data.value = AuthState(id = id, token = token)
        }
    }

    fun setAuth(authState: AuthState) {
        _data.value = authState
    }

    fun clearAuth() {
        _data.value = AuthState()
    }

    companion object {

        private var INSTANCE: AppAuth? = null

        fun getInstance() : AppAuth = requireNotNull(INSTANCE) {
            "AppAuth init should called"
        }

        fun init(context: Context) {
            INSTANCE = AppAuth(context)
        }

    }

}