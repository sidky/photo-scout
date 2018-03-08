package com.github.sidky.photoscout.data.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PagedList
import android.support.annotation.GuardedBy
import android.util.Log
import com.github.sidky.photoscout.api.flickr.BoundingBox
import com.github.sidky.photoscout.api.flickr.FlickrService
import com.github.sidky.photoscout.api.flickr.PhotoListResponse
import com.github.sidky.photoscout.data.converter.FlickrPhotoConverter
import com.github.sidky.photoscout.data.converter.FlickrPhotoListConverter
import com.github.sidky.photoscout.data.converter.FlickrPhotoSizeConverter
import com.github.sidky.photoscout.data.converter.FlickrPhotoSizeListConverter
import com.github.sidky.photoscout.data.dao.PhotoDao
import com.github.sidky.photoscout.data.entity.Photo
import com.github.sidky.photoscout.data.entity.PhotoSize
import com.github.sidky.photoscout.data.entity.PhotoWithSize
import kotlinx.coroutines.experimental.async
import retrofit2.Call
import timber.log.Timber

class PhotoBoundaryCallback(
        val query: String?,
        val boundingBox: BoundingBox?,
        val dao: PhotoDao,
        val flickrApi: FlickrService,
        val queue: RequestQueue,
        val pageSize: Int = 10): PagedList.BoundaryCallback<PhotoWithSize>() {

    private var _cancelled: Boolean = false
    var cancelled: Boolean
        get() = synchronized(this) {
            _cancelled
        }
        set(value) = synchronized(this) {
            _cancelled = value
        }

    var nextPage: Int = 1
    var totalPage: Int = -1

    init {
        loadPage(1)
    }

    override fun onZeroItemsLoaded() {
        loadPage(1)
    }

    override fun onItemAtEndLoaded(itemAtEnd: PhotoWithSize) {
        if (totalPage > 0 && totalPage >= nextPage) {
            Log.i("Boundary", "Load page: $nextPage")
            loadPage(nextPage)
        } else {
            Log.i("Boundary", "No more page to load")
        }
    }

    fun loadPage(page: Int) {
        queue.submit(networkCall(page), object : RequestCallback {
            override fun succeed(response: PhotoListResponse) {
                if (!cancelled) {
                    addToDb(response)
                }
            }

            override fun failed(error: NetworkError) {
                // Retry?
            }

        })
    }

    override fun onItemAtFrontLoaded(itemAtFront: PhotoWithSize) {
        super.onItemAtFrontLoaded(itemAtFront)
    }

    fun networkCall(page: Int): Call<PhotoListResponse> {
        return if (query == null && boundingBox == null) {
            flickrApi.interesting(page = page)
        } else {
            flickrApi.search(text = query, boundingBox = boundingBox)
        }
    }

    private fun addToDb(response: PhotoListResponse) {
        totalPage = response.photos.pages
        val page = response.photos.page

        Log.i("Boundary", "Page: $page")
        if (page + 1 > nextPage) {
            nextPage = page + 1
        }
        val photos = FlickrPhotoListConverter.convert(
                list = response.photos.photo,
                pageSize =  response.photos.perpage,
                page = response.photos.page)
        val sizes = FlickrPhotoSizeListConverter.convert(response.photos.photo)
        dao.addPhotosWithSizes(photos, sizes, page == 1)
    }
}
