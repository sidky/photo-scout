package com.github.sidky.photoscout

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.sidky.data.repository.PhotoDetail
import com.github.sidky.data.repository.PhotoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

class PhotoDetailViewModel(private val repository: PhotoRepository) : ViewModel() {
    val detailLiveData = MutableLiveData<PhotoDetail>()

    val detail: LiveData<PhotoDetail>
        get() = detailLiveData

    var photoId: String = ""

    fun load(photoId: String) {
        this.photoId = photoId
        CoroutineScope(EmptyCoroutineContext).launch {
            val detail = repository.detail(photoId)

            GlobalScope.launch(Dispatchers.Main) {
                detailLiveData.value = detail
            }
        }
    }

    suspend fun bookmark(photoId: String, lifecycleOwner: LifecycleOwner) {
        val successful = repository.bookmarkPhoto(photoId, lifecycleOwner)

        if (successful) {
            load(photoId)
        }
    }
}