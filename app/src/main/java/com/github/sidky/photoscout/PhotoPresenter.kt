package com.github.sidky.photoscout

import android.arch.paging.PagedList
import com.github.sidky.photoscout.api.flickr.BoundingBox
import com.github.sidky.photoscout.data.entity.PhotoWithSize
import com.github.sidky.photoscout.data.repository.FlickrPhotoRepository
import com.github.sidky.photoscout.data.repository.PagedListObservable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class PhotoPresenter(val repository: FlickrPhotoRepository) {

    private val actions: PublishSubject<Action> = PublishSubject.create()
    private val stateObservable: Observable<ScreenState>

    private val compositeDisposable = CompositeDisposable()

    init {
        stateObservable = actions.serialize()
                .observeOn(Schedulers.computation())
                .scan(ScreenState.INIT, { prevState: ScreenState, action: Action ->
                    when (action) {
                        is Action.Search -> prevState.copy(query = action.query)
                        is Action.ClearQuery -> prevState.copy(query = null)
                        is Action.MoveMap -> prevState.copy(region = action.boundingBox)
                    }
                })

        val actionDisposable = stateObservable
                .observeOn(Schedulers.io())
                .flatMap { repository.getPhotos(query = it.query, boundingBox = it.region).observable() }
                .observeOn(Schedulers.io())
                .map { _photoList.onNext(it) }
                .subscribe()

        compositeDisposable.add(actionDisposable)
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

    fun search(query: String) = actions.onNext(Action.Search(query))
    fun clearSearch() = actions.onNext(Action.ClearQuery())
    fun moveMap(boundingBox: BoundingBox) = actions.onNext(Action.MoveMap(boundingBox))

    data class ScreenState(val query: String?, val region: BoundingBox? = null) {
        companion object {
            val INIT = ScreenState(query = null)
        }
    }

    sealed class Action {
        class Search(val query: String) : Action()
        class ClearQuery(): Action()
        class MoveMap(val boundingBox: BoundingBox): Action()
    }

    fun clear() {
        compositeDisposable.clear()
    }
}