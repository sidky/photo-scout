package com.github.sidky.photoscout

import androidx.lifecycle.*
import androidx.paging.PagedList
import com.github.sidky.data.dao.PhotoThumbnail
import com.github.sidky.data.repository.PhotoRepository
import timber.log.Timber

class PhotoListViewModel(private val repository: PhotoRepository) : ViewModel() {

    private val actionLiveData = MediatorLiveData<Unit>()

    val photoLiveData = MutableLiveData<PagedList<PhotoThumbnail>>()

    private var listLiveData: LiveData<PagedList<PhotoThumbnail>>? = null
        set(value) {
            val prev = field
            if (prev != null) {
                actionLiveData.removeSource(prev)
            }
            if (value != null) {
                actionLiveData.addSource(value) {
                    Timber.d("Loaded")
                    photoLiveData.value = it
                }
            }
            field = value
        }

    fun loadInteresting() {
        val listing = repository.loadInteresting()

        listLiveData = listing.pagedList
    }

    fun search(query: String) {
        val listing = repository.loadSearch(query)

        listLiveData = listing.pagedList
    }

    fun attach(lifecycleOwner: LifecycleOwner) {
        actionLiveData.observe(lifecycleOwner, Observer<Unit> { })
    }

    fun openDetail() {
    }
}