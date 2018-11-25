package com.github.sidky.photoscout.data

import androidx.annotation.Dimension
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.apollographql.apollo.ApolloClient
import com.github.sidky.photoscout.data.paging.InterestingRequestHelper
import com.github.sidky.photoscout.data.paging.PhotoBoundaryCallback
import timber.log.Timber

data class Listing(val pagedList: LiveData<PagedList<PhotoWithURL>>)

class PhotoRepository(private val apolloClient: ApolloClient, private val dao: PhotoDAO) {

    val pagedListConfig = PagedList.Config.Builder()
        .setEnablePlaceholders(true)
        .setPageSize(10)
        .build()

    var maxWidth: Int = 300
    var maxHeight: Int = 300

    fun setMaxDimension(dimension: Int) {
        maxWidth = dimension
        maxHeight = dimension
    }

    private val comparator = Comparator<SizedURL> { o1, o2 ->
        if (o1.width == o2.width) {
            o1.height - o2.height
        } else {
            o1.width - o2.width
        }
    }

    fun loadInteresting(): Listing {
        val boundaryCallback = PhotoBoundaryCallback<PhotoWithURL>(apolloClient, dao, InterestingRequestHelper())

        Timber.e("Inside repository")

        return Listing(toLiveData(dao.photos(), boundaryCallback))
    }

    private fun <T>toLiveData(factory: DataSource.Factory<Int, T>, boundaryCallback: PhotoBoundaryCallback<T>): LiveData<PagedList<T>> {
        return LivePagedListBuilder(factory, pagedListConfig)
            .setBoundaryCallback(boundaryCallback)
            .build()
    }
}