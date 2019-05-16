package com.github.sidky.data.repository

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.work.*
import com.apollographql.apollo.ApolloClient
import com.github.sidky.data.converter.ApolloDetailToModelConverter
import com.github.sidky.data.dao.LoadingState
import com.github.sidky.data.dao.PhotoDAO
import com.github.sidky.data.dao.PhotoThumbnail
import com.github.sidky.data.dao.ThumbnailWithLocation
import com.github.sidky.data.operation.BookmarkPhoto
import com.github.sidky.data.paging.*
import com.github.sidky.photoscout.graphql.PhotoDetailQuery
import kotlin.coroutines.suspendCoroutine


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

    suspend fun bookmarkPhoto(id: String, lifecycle: LifecycleOwner): Boolean {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .setRequiresDeviceIdle(false)
            .build()

        val req = OneTimeWorkRequestBuilder<BookmarkPhoto>().setConstraints(constraints)
            .setInputData(Data.Builder().putString("id", id).build())
            .build()


        return suspendCoroutine<Boolean> { cont ->
            WorkManager.getInstance().enqueue(req).state.observe(lifecycle, Observer<Operation.State> {
                when (it) {
                    is Operation.State.SUCCESS -> cont.resumeWith(Result.success(true))
                    is Operation.State.FAILURE -> cont.resumeWith(Result.success(false))
                }
            })
        }
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