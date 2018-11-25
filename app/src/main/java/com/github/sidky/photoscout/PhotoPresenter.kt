package com.github.sidky.photoscout

import androidx.lifecycle.*
import androidx.paging.PagedList
import com.github.sidky.photoscout.data.PhotoRepository
import com.github.sidky.photoscout.data.PhotoWithURL
import com.github.sidky.photoscout.data.model.Photo
import timber.log.Timber

class PhotoPresenter(private val repository: PhotoRepository) : ViewModel() {

    private val actionLiveData = MediatorLiveData<Unit>()

    val photoLiveData by lazy {
        MutableLiveData<PagedList<PhotoWithURL>>()
    }

    fun setMaxDimension(dimension: Int) = repository.setMaxDimension(dimension)

    fun loadInteresting() {
        val listing = repository.loadInteresting()

        actionLiveData.addSource(listing.pagedList, {
            Timber.e("Loaded")
            photoLiveData.value = it
        })
    }

    fun attach(owner: LifecycleOwner) {
        actionLiveData.observe(owner, Observer<Unit> { })
    }
}