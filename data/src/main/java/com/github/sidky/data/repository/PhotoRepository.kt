package com.github.sidky.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.apollographql.apollo.ApolloClient
import com.github.sidky.data.converter.ApolloDetailToModelConverter
import com.github.sidky.data.dao.LoadingState
import com.github.sidky.data.dao.PhotoDAO
import com.github.sidky.data.dao.PhotoThumbnail
import com.github.sidky.data.dao.ThumbnailWithLocation
import com.github.sidky.data.paging.*
import com.github.sidky.photoscout.graphql.PhotoDetailQuery


data class Listing(val pagedList: LiveData<PagedList<PhotoThumbnail>>)

class PhotoRepository(
    private val apolloClient: ApolloClient,
    private val dao: PhotoDAO,
    private val loadingState: LoadingState,
    private val detailConverter: ApolloDetailToModelConverter
) {

    val pagedListConfig = PagedList.Config.Builder()
        .setEnablePlaceholders(true)
        .setPageSize(50)
        .setPrefetchDistance(0)
        .build()

    fun loadInteresting(): Listing {
        loadingState.resetToInteresting()

        val cb = InterestingPhotoBoundaryCallback(loadingState)

        val liveData = LivePagedListBuilder(dao.thumbnails(200), pagedListConfig).setBoundaryCallback(cb).build()
        cb.firstPage()
        return Listing(liveData)
    }

    fun loadInteresting(location: BoundingBox): Listing {
        loadingState.resetToInteresting(location)

        val cb = InterestingAtLocationPhotoBoundaryCallback(location, loadingState)

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

    fun thumbnailsForMap(): LiveData<List<ThumbnailWithLocation>> = dao.allPhotoWithLocation(100)

    suspend fun detail(photoId: String): PhotoDetail? {
        val query = PhotoDetailQuery.builder().photoId(photoId).build()
        val response = apolloClient.query(query).execute()

        return detailConverter.convertNullable(response.data()?.detail())
    }

    suspend fun clear() {
        dao.deleteAll()
    }
}