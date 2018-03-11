package com.github.sidky.photoscout

import android.arch.paging.PagedList
import com.github.sidky.photoscout.api.flickr.BoundingBox
import com.github.sidky.photoscout.data.entity.PhotoWithSize
import com.github.sidky.photoscout.data.repository.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class PhotoPresenter(val repository: FlickrPhotoRepository, val requestQueue: RequestQueue) {

    private val actions: PublishSubject<Action> = PublishSubject.create()
    private val stateObservable: Flowable<ScreenState>
    private val compositeDisposable = CompositeDisposable()

    private var boundingBox: BoundingBox? = null

    private val uiActionObservable: PublishSubject<UserAction> = PublishSubject.create()
    private val uiStateObservable: Flowable<UiState>

    init {
        stateObservable = actions.serialize().toFlowable(BackpressureStrategy.BUFFER)
                .observeOn(Schedulers.computation())
                .scan(ScreenState.INIT, { prevState: ScreenState, action: Action ->
                    when (action) {
                        is Action.Search -> prevState.copy(query = action.query, region = boundingBox)
                        is Action.ClearQuery -> prevState.copy(query = null)
                        is Action.Refresh -> {
                            prevState.copy(region = boundingBox)
                        }
                    }
                })

        val actionDisposable = stateObservable
                .observeOn(Schedulers.io())
                .flatMap {
                    currentList = repository.getPhotos(query = it.query, boundingBox = it.region)
                    currentList?.observable()?.toFlowable(BackpressureStrategy.BUFFER)
                }
                .observeOn(Schedulers.io())
                .map {
                    _photoList.onNext(it)
                }
                .subscribe()

        val networkDisposable = requestQueue
                .stateObservable
                .observeOn(Schedulers.computation())
                .subscribe {
                    uiActionObservable.onNext(UserAction.NetworkState(it))
                }

        uiStateObservable = uiActionObservable
                .observeOn(Schedulers.computation())
                .scan(UiState.INIT, { state, action ->
                    when (action) {
                        is UserAction.Switch -> {
                            val newScreen = if (state.screen == Screen.LIST) {
                                Screen.MAP
                            } else {
                                Screen.LIST
                            }
                            state.copy(screen = newScreen)
                        }
                        is UserAction.NetworkState -> {
                            state.copy(isLoading = action.queueState.loading, error = action.queueState.lastError)
                        }
                    }
                }).toFlowable(BackpressureStrategy.LATEST)

        compositeDisposable.addAll(actionDisposable, networkDisposable)
    }

    private val _photoList: BehaviorSubject<PagedList<PhotoWithSize>> = BehaviorSubject.create()

    private var currentList: PagedListObservable? = null

    val photos: Flowable<PagedList<PhotoWithSize>>
        get() = _photoList.serialize().toFlowable(BackpressureStrategy.LATEST)

    fun screenState(): Flowable<UiState> = uiStateObservable.serialize()

    fun refresh() = actions.onNext(Action.Refresh())
    fun search(query: String) = actions.onNext(Action.Search(query))
    fun clearSearch() = actions.onNext(Action.ClearQuery())
    fun moveMap(boundingBox: BoundingBox) {
        this.boundingBox = boundingBox
    }
    fun switch() = uiActionObservable.onNext(UserAction.Switch())

    data class ScreenState(val query: String?,
                           val region: BoundingBox? = null) {
        companion object {
            val INIT = ScreenState(query = null)
        }
    }

    sealed class Action {
        class Search(val query: String) : Action()
        class ClearQuery(): Action()
        class Refresh(): Action()
    }

    fun clear() {
        compositeDisposable.clear()
    }

    enum class Screen {
        LIST, MAP
    }

    sealed class UserAction {
        class Switch(): UserAction()
        class NetworkState(val queueState: QueueState): UserAction()
    }

    data class UiState(val screen: Screen, val isLoading: Boolean, val error: NetworkError?) {
        companion object {
            val INIT = UiState(Screen.LIST, false, null)
        }
    }
}