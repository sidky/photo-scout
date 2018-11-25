package com.github.sidky.photoscout.data.paging

import androidx.paging.PagedList
import com.apollographql.apollo.ApolloClient
import com.github.sidky.photoscout.data.*
import com.github.sidky.photoscout.data.model.PhotoResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.github.sidky.photoscout.data.model.Photo as PhotoModel

class PhotoBoundaryCallback<T>(private val client: ApolloClient,
                            private val dao: PhotoDAO,
                            private val helper: RequestHelper<*>) : PagedList.BoundaryCallback<T>(), RequestCallback {
    private val mu = Mutex()
    private var nextPage: Int? = null
    private var isLoading = false
    private var hasNext = true
    private var failedRequests = 0

    init {
        loadNext()
    }

    override fun onZeroItemsLoaded() {
        loadNext()
    }

    override fun onItemAtEndLoaded(itemAtEnd: T) {
        loadNext()
    }

    private fun loadNext() = runBlocking {
        if (hasNext) {
            val isLocked = mu.withLock {
                if (!isLoading) {
                    isLoading = true
                    true
                } else {
                    false
                }
            }
            if (isLocked) {
                helper.load(client, nextPage, this@PhotoBoundaryCallback)
            }
        }
    }

    override fun onSuccess(data: PhotoResponse) = runBlocking {
        mu.withLock {
            isLoading = false
        }
        failedRequests = 0
        updatePhotos(data.photos, nextPage == null)
        if (!data.pagination.hasNext) {
            hasNext = false
        } else {
            nextPage = data.pagination.next
        }
    }

    override fun onFailure() = runBlocking {
        mu.withLock {
            isLoading = false
        }
        failedRequests++

        if (failedRequests < 3) {
            async {
                delay(1000)
                loadNext()
            }
        }
    }

    private fun updatePhotos(photos: List<PhotoModel>, firstPage: Boolean) {
        val photoWithURLs = photos.map {
            val urls = it.urls.map {
                SizedURL(photoId = 0L, url = it.url, width = it.width, height = it.height)
            }

            val location = it.location?.let {
                Location(it.longitude, it.latitude, it.accuracy)
            }

            val photo = Photo(photoId = it.id, ownerName = it.ownerName, location = location)

            PhotoWithURL(photo, urls)
        }

        if (firstPage) {
            dao.setPhotos(photoWithURLs)
        } else {
            dao.addPhotos(photoWithURLs)
        }
    }
}