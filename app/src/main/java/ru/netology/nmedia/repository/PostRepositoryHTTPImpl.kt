package ru.netology.nmedia.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.*
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.api.toPostWithFullUrl
import ru.netology.nmedia.api.toPostsWithFullUrl
import ru.netology.nmedia.auth.LoginUser
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AuthState
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.*
import ru.netology.nmedia.entity.toPost
import ru.netology.nmedia.entity.toPostEntity
import ru.netology.nmedia.entity.toReadedPostEntity
import ru.netology.nmedia.entity.toReadedPostsEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.exception.AppError
import java.io.File


class PostRepositoryHTTPImpl(
    private val postDao: PostDao
) : PostRepository {

    override val data = postDao.getAll()
        .map(List<PostEntity>::toPost)
        .flowOn(Dispatchers.Default)

    override suspend fun getAll() {

        val result = PostsApi.retrofitService.getAll()

        if (!result.isSuccessful) {
            error("Response code: ${result.code()}")
        }

        val postsOnServer = result.body() ?: error("Body is null")
        val posts = postsOnServer.toPostsWithFullUrl()
        postDao.insert(posts.toPostEntity())
        postDao.insertReadingPosts(posts.toReadedPostEntity())

    }

    override fun getNeverCount(): Flow<Int> = flow {
        while (true) {

            val result = PostsApi.retrofitService.getNewer(postDao.getMaxId())
            if (!result.isSuccessful) {
                error("Response code: ${result.code()}")
            }
            val postsOnServer = result.body() ?: error("Body is null")
            val posts = postsOnServer.toPostsWithFullUrl()
            postDao.insert(posts.toPostEntity())

            val countResult = postDao.getNeverCount()

            emit(countResult)
            delay(10_000)
        }


    }
        .catch { e ->
            val err = AppError.from(e)
            throw CancellationException()
        }
        .flowOn(Dispatchers.Default)

    override suspend fun getById(id: Long): Post {

        val result = PostsApi.retrofitService.getById(id)

        if (!result.isSuccessful) {
            error("Response code: ${result.code()}")
        }

        val postOnServer = result.body() ?: error("Body is null")
        val post = postOnServer.toPostWithFullUrl()
        postDao.insertPost(post.toPostEntity())

        return post

    }

    override suspend fun readAllPosts() {
        postDao.insertReadingPosts(
            postDao.getUnreadedPosts().toReadedPostsEntity()
        )
        getNeverCount()
    }

    override suspend fun loginAsUser(loginUser: LoginUser) : AuthState {
        val result = PostsApi.retrofitService.loginAsUser(loginUser.login, loginUser.password)

        if (!result.isSuccessful) {
            error("Response code: ${result.code()}")
        }

        val authState = result.body() ?: error("Body is null")

        return authState

    }

    override suspend fun likeById(id: Long) {

        val postInBase = postDao.getById(id)
        val likedPost = postInBase.copy(
            likedByMe = !postInBase.likedByMe,
            likes = if (postInBase.likedByMe) {postInBase.likes - 1} else {postInBase.likes + 1}
        )

        postDao.insertPost(likedPost)

        val result =
            if (postInBase.likedByMe) { PostsApi.retrofitService.dislikeById(id) } else { PostsApi.retrofitService.likeById(id) }
        if (!result.isSuccessful) {
            error("Response code: ${result.code()}")
        }
    }

    override suspend fun shareById(id: Long) {

    }

    override suspend fun removeById(id: Long) {
        postDao.removeById(id)
        val result = PostsApi.retrofitService.removeById(id)
        if (!result.isSuccessful) {
            error("Response code: ${result.code()}")
        }

    }

    override suspend fun save(post: Post) {
        val result = PostsApi.retrofitService.save(post)
        if (!result.isSuccessful) {
            error("Response code: ${result.code()}")
        }
        val refreshPostOnServer = result.body() ?: error("Body is null")
        val refreshPost = refreshPostOnServer.toPostWithFullUrl()
        postDao.insertPost(refreshPost.toPostEntity())
        postDao.insertReadingPost(refreshPost.toReadedPostEntity())
    }

    override suspend fun saveWithAttachment(post: Post, file: File?) {
        if (file == null) return save(post)

        val media = upload(file)
        val result = PostsApi.retrofitService.save(
            post.copy(
                attachment = Attachment(
                    url = media.id,
                    description = "",
                    type = AttachmentType.IMAGE)
            )
        )
        if (!result.isSuccessful) {
            error("Response code: ${result.code()}")
        }
        val refreshPostOnServer = result.body() ?: error("Body is null")
        val refreshPost = refreshPostOnServer.toPostWithFullUrl()

        postDao.insertPost(refreshPost.toPostEntity())
        postDao.insertReadingPost(refreshPost.toReadedPostEntity())
    }

    private suspend fun upload(file: File) : Media {
        try {

            val media = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
            val result = PostsApi.retrofitService.upload(media)

            if (!result.isSuccessful) {
                error("Response code: ${result.code()}")
            }

            return result.body() ?: error("Body is null")

        } catch (e: Exception) {
            val err = AppError.from(e)
            error(err)
        }
    }


}