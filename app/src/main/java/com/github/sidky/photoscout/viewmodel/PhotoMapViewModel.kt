package com.github.sidky.photoscout.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagedList
import com.github.sidky.photoscout.data.PhotoDAO
import com.github.sidky.photoscout.data.PhotoWithURL
import com.github.sidky.photoscout.util.DiffCallback
import com.github.sidky.photoscout.util.KeyExtractor
import com.github.sidky.photoscout.util.SetDiffUtil
import kotlinx.coroutines.*

class PhotoMapViewModel(private val dao: PhotoDAO): ViewModel() {

    private val extractor = object : KeyExtractor<PhotoWithURL> {
        override fun key(t: PhotoWithURL): String = t.photo.photoId
    }

    fun attach(): LiveData<List<PhotoWithURL>> {
        return Transformations.map(dao.photosListLiveData()) {
            it.filter { it.photo.location != null }
        }
    }

    fun subscribeDiff(lifecycleOwner: LifecycleOwner, callback: DiffCallback<PhotoWithURL>) {
        val diffUtil = SetDiffUtil(extractor, callback)
        dao.photosListLiveData().observe(lifecycleOwner, Observer<List<PhotoWithURL>> {
            GlobalScope.launch {
                diffUtil.update(it)
            }
        })
    }
}

