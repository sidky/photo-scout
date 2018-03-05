package com.github.sidky.photoscout

import android.arch.paging.PagedList
import com.github.sidky.photoscout.data.entity.Photo
import com.github.sidky.photoscout.data.entity.PhotoWithSize
import com.github.sidky.photoscout.data.repository.FlickrPhotoRepository
import com.github.sidky.photoscout.data.repository.PagedListObservable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.Callable
import kotlin.math.sin

class PhotoPresenter(val repository: FlickrPhotoRepository) {

    init {
        Completable.fromAction { refresh() }.andThen(interesting())
                .subscribeOn(Schedulers.io()).subscribe()
    }

    private val _photoList: PublishSubject<PagedList<PhotoWithSize>> = PublishSubject.create()

    private var currentList: PagedListObservable? = null

    val photos: Observable<PagedList<PhotoWithSize>>
        get() = _photoList.serialize()

    fun interesting(): Completable {
        currentList = repository.getPhotos(query = null, boundingBox = null)
        val observable = currentList
                ?.observable()
                ?.observeOn(Schedulers.io())
                ?.map { _photoList.onNext(it) }
        return Completable.fromObservable(observable)
    }

    fun refresh() = currentList?.refresh()
}