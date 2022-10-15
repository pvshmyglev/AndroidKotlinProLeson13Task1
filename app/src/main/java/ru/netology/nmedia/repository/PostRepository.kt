package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.auth.LoginUser
import ru.netology.nmedia.dto.AuthState
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {

    val data : Flow<List<Post>>
    suspend fun getAll()
    fun getNeverCount() : Flow<Int>
    suspend fun likeById(id: Long)
    suspend fun shareById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, file: File?)
    suspend fun getById(id: Long) : Post
    suspend fun readAllPosts()
    suspend fun loginAsUser(loginUser: LoginUser) : AuthState

}