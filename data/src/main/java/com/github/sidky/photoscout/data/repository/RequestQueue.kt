package com.github.sidky.photoscout.data.repository

import android.support.annotation.GuardedBy
import com.github.sidky.photoscout.api.flickr.PhotoListResponse
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.Call

data class NetworkError(val code: Int, val body: String?) : Throwable()

data class QueueState(val loading: Boolean,
                      val lastRequestSucceeded: Boolean = true,
                      val lastError: NetworkError? = null) {
}


interface RequestCallback {
    fun succeed(response: PhotoListResponse)
    fun failed(error: NetworkError)
}

class RequestQueue(private val scheduler: Scheduler) {
    private val subject = PublishSubject.create<Request>()

    private val _stateSubject = BehaviorSubject.create<QueueState>()

    val stateObservable: Observable<QueueState>
        get() = _stateSubject.serialize()

    data class Request(val req: Call<PhotoListResponse>, val callback: RequestCallback)

    private val compositeDisposable = CompositeDisposable()

    fun submit(call: Call<PhotoListResponse>, callback: RequestCallback) {
        subject.onNext(Request(call, callback))
    }

    fun start() {
        if (compositeDisposable.isDisposed) {
            throw IllegalStateException("Queue has already been unsubscribed")
        }
        _stateSubject.onNext(QueueState(false))

        val disposable = subject.toSerialized()
                .observeOn(scheduler)
                .subscribe { request ->
                    _stateSubject.onNext(_stateSubject.value.copy(loading = true))
                    val response = request.req.execute()
                    if (response.isSuccessful && response.code() == 200) {
                        _stateSubject.onNext(QueueState(false,
                                true,
                                null))
                        val list = response.body()
                        if (list != null) {
                            request.callback.succeed(list)
                        }
                    } else {
                        val error = NetworkError(
                                response.code(),
                                response.errorBody()?.string())
                        _stateSubject.onNext(QueueState(
                                false,
                                false,
                                error))
                        request.callback.failed(error)
                    }
        }
        compositeDisposable.add(disposable)
    }

    fun shutdown() {
        compositeDisposable.dispose()
    }
}