package com.github.sidky.photoscout.details

import com.github.sidky.photoscout.api.flickr.FlickrService
import com.github.sidky.photoscout.api.flickr.PhotoInfo
import com.github.sidky.photoscout.data.repository.FlickrPhotoRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PhotoDetailsPresenter(private val flickrRepository: FlickrPhotoRepository) {

    fun getInfo(photoDetails: PhotoDetails): Single<PhotoInfo> =
            flickrRepository.getPhotoInfo(photoDetails.id)
                    .subscribeOn(Schedulers.io())
}