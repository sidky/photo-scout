package com.github.sidky.photoscout.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.github.sidky.photoscout.data.PhotoRepository
import com.github.sidky.photoscout.data.model.PhotoDetail

class PhotoDetailViewModel(val repository: PhotoRepository) : ViewModel() {

    val details: MediatorLiveData<PhotoDetail?> = MediatorLiveData()

    fun loadDetails(photoId: String) {
        val source = repository.getDetails(photoId)
        details.addSource(source, {})
    }
}