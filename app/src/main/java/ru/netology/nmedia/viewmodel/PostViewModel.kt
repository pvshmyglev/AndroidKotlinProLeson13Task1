package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryHTTPImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File

class PostViewModel (application: Application) : AndroidViewModel(application),
    PostInteractionCommands {

    private val emptyAttachment = MediaModel()

    private val repository : PostRepository = PostRepositoryHTTPImpl(
        AppDb.getInstance(application).postDao()
    )

    val data : LiveData<FeedModel> = AppAuth.getInstance().data.flatMapLatest {(id, _) ->
        repository.data.map{posts ->
            posts.map {
                it.copy(ownedByMe = it.authorId == id)
            }

        }
            .map(::FeedModel)
            .catch { e ->
                e.printStackTrace()
            }
    }
        .asLiveData(Dispatchers.Default)

    private val _media = MutableLiveData<MediaModel>()
    val media : LiveData<MediaModel>
        get() = _media

    private val _state = MutableLiveData(FeedModelState())
    val state : LiveData<FeedModelState>
        get() = _state

    val never: LiveData<Int> = data.switchMap {
        repository.getNeverCount().asLiveData(Dispatchers.Default)
    }

    private val emptyPost = Post(
        0L,
        "",
        0L,
        "",
        "",
        "",
        0,
        false,
        0,
        0,
        0
    )

    val editedPost = MutableLiveData(emptyPost)
    val openedPost = MutableLiveData(emptyPost)
    val openedAttachment = MutableLiveData(emptyPost)

    private val _postUpdated = SingleLiveEvent<Post>()
    val postUpdated: LiveData<Post>
        get() = _postUpdated

    private val _needScrolling = SingleLiveEvent<Boolean>()
    val needScrolling: LiveData<Boolean>
        get() = _needScrolling

    private val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: MutableLiveData<String>
        get() = _errorMessage

    fun updatedPost(post: Post) {

        viewModelScope.launch {

            try {
                _state.value = FeedModelState(refreshing = true)
                repository.save(post)
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

    }

    fun loadPosts() {

       viewModelScope.launch {

           try {
               _state.value = FeedModelState(loading = true)
               repository.getAll()
               _state.value = FeedModelState()
           } catch (e: Exception) {
               _state.value = FeedModelState(error = true)
           }
       }
    }

    fun refreshPosts() {

        viewModelScope.launch {

            try {
                _state.value = FeedModelState(refreshing = true)
                repository.getAll()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    private fun setObserveEditOpenPost(id: Long) {

        if (editedPost.value?.id != 0L && editedPost.value?.id == id) {
            data.value?.posts?.map { post ->
                if (post.id == editedPost.value?.id) { editedPost.value = post }
            }
        }

        if (openedPost.value?.id != 0L && openedPost.value?.id == id) {
            data.value?.posts?.map { post ->
                if (post.id == openedPost.value?.id) { openedPost.value = post }
            }
        }
    }

    override fun onLike(post: Post) {

        viewModelScope.launch {
            try {
                _state.value = FeedModelState(refreshing = true)
                repository.likeById(post.id)
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }



    override fun onShare(post: Post) {
        //TODO
    }

    override fun onRemove(post: Post) {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(refreshing = true)
                repository.removeById(post.id)
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    override fun onSaveContent(newContent: String) {
        viewModelScope.launch {
            val text = newContent.trim()
            editedPost.value?.let { thisEditedPost ->
                if (thisEditedPost.content != text) {
                    val postForSaved = thisEditedPost.copy(content = text)
                    when (val attachment = media.value) {
                        emptyAttachment -> repository.save(postForSaved)
                        else -> repository.saveWithAttachment(postForSaved, attachment?.file)
                    }
                }
                editedPost.value = emptyPost
                setObserveEditOpenPost(thisEditedPost.id)
            }
        }
    }

    override fun readNeverPosts() {
        viewModelScope.launch {
            repository.readAllPosts()
            _needScrolling.postValue(true)
        }

    }

    override fun onEditPost(post: Post) {
        editedPost.value = post
    }

    override fun onCancelEdit() {
        editedPost.value = emptyPost
    }

    override fun onOpenPost(post: Post) {
        openedPost.value = post
    }

    override fun onOpenAttachment(post: Post) {
        openedAttachment.value = post
    }

    override fun onCancelOpen() {
        openedPost.value = emptyPost
    }

    override fun onCancelOpenAttachment() {
        openedAttachment.value = emptyPost
    }

    fun saveAttachment(uri: Uri?, file: File?) {
        _media.value = MediaModel(uri, file)
    }

    fun clearAttachment() {
        _media.value = emptyAttachment
    }

}
