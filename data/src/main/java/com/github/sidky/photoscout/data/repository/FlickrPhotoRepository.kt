package com.github.sidky.photoscout.data.repository

import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.github.sidky.photoscout.api.flickr.*
import com.github.sidky.photoscout.data.converter.FlickrPhotoConverter
import com.github.sidky.photoscout.data.converter.FlickrPhotoSizeConverter
import com.github.sidky.photoscout.data.dao.PhotoDao
import com.github.sidky.photoscout.data.entity.Photo
import com.github.sidky.photoscout.data.entity.PhotoSize
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.HTTP
import java.util.concurrent.Executor

class FlickrPhotoRepository(
        val dao: PhotoDao,
        val flickrApi: FlickrService,
        val requestQueue: RequestQueue,
        val mainThreadExecutor: Executor,
        val backgroundThreadExecutor: Executor) {

    fun getPhotos(query: String? = null, boundingBox: BoundingBox? = null): PagedListObservable {
        val boundaryCallback = PhotoBoundaryCallback(
                query = query,
                boundingBox = boundingBox,
                dao = dao,
                flickrApi = flickrApi,
                queue = requestQueue)
        dao.clearPhotos()
        val dataSourceFactory = dao.photos()
        return PagedListObservable(dao, dataSourceFactory, boundaryCallback, mainThreadExecutor, backgroundThreadExecutor)
    }

    fun getPhotoInfo(photoId: Long) =
        Single.create<PhotoInfo> {
            val response = flickrApi.info(photoId = photoId).execute()

            if (response.isSuccessful) {
                val info = response?.body()?.photo
                if (response?.code() == 200 && info != null) {
                    it.onSuccess(info)
                } else {
                    it.onError(IllegalStateException("Invalid response: code=${response?.code()}, body: ${response?.body()}"))
                }
            } else {
                it.onError(NetworkError(response.code(), response.errorBody()?.string()))
            }
        }
}
