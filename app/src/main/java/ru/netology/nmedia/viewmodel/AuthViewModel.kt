package ru.netology.nmedia.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.LoginUser
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryHTTPImpl

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository : PostRepository = PostRepositoryHTTPImpl(
        AppDb.getInstance(application).postDao()
    )

    val data = AppAuth.getInstance()
        .data
        .asLiveData(Dispatchers.Default)

    val authorized: Boolean
        get() = data.value?.token != null

    fun loginAsUser(login:String, password: String) {
        viewModelScope.launch {

            try {

                val authState = repository.loginAsUser(LoginUser(login = login, password = password))
                AppAuth.getInstance().setAuth(authState)

            } catch (e: Exception) {
                Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_SHORT).show()
            }
        }

    }


}