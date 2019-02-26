package com.github.sidky.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.github.sidky.data.dao.LoadingState
import com.github.sidky.data.dao.PhotoDAO
import com.github.sidky.data.dao.PhotoThumbnail
import com.github.sidky.data.paging.InterestingPhotoBoundaryCallback
import com.github.sidky.data.paging.SearchPhotoBoundaryCallback

data class Listing(val pagedList: LiveData<PagedList<PhotoThumbnail>>)

class PhotoRepository(
    private val dao: PhotoDAO,
    private val loadingState: LoadingState
) {

    val pagedListConfig = PagedList.Config.Builder()
        .setEnablePlaceholders(true)
        .setPageSize(50)
        .setPrefetchDistance(0)
        .build()

    fun loadInteresting(): Listing {
        loadingState.resetToInteresring()

        val cb = InterestingPhotoBoundaryCallback(loadingState)

        val liveData = LivePagedListBuilder(dao.thumbnails(200), pagedListConfig).setBoundaryCallback(cb).build()
        cb.firstPage()
        return Listing(liveData)
    }

    fun loadSearch(query: String): Listing {
        loadingState.resetToSearch(query)

        val cb = SearchPhotoBoundaryCallback(query, loadingState)

        val liveData = LivePagedListBuilder(dao.thumbnails(200), pagedListConfig).setBoundaryCallback(cb).build()
        cb.firstPage()
        return Listing(liveData)
    }
}