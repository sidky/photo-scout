package com.github.sidky.photoscout.data.repository

import android.arch.paging.DataSource
import android.arch.paging.PagedList
import android.os.Handler
import android.util.Log
import com.github.sidky.photoscout.data.dao.PhotoDao
import com.github.sidky.photoscout.data.entity.Photo
import com.github.sidky.photoscout.data.entity.PhotoWithSize
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class PagedListObservable(val dao: PhotoDao,
                          val datasourceFactory: DataSource.Factory<Int, PhotoWithSize>,
                          val callback: PhotoBoundaryCallback,
                          val mainThreadExecutor: Executor,
                          val backgroundThreadExecutor: Executor): Disposable {

    private val disposed: AtomicBoolean = AtomicBoolean()
    private val pagedListSubject: BehaviorSubject<PagedList<PhotoWithSize>> = BehaviorSubject.create()

    private var _dataSource: DataSource<Int, PhotoWithSize>? = null

    private fun newDataSource(): DataSource<Int, PhotoWithSize> {
        _dataSource?.removeInvalidatedCallback(invalidationCallback)
        val dataSource = datasourceFactory.create()
        dataSource.addInvalidatedCallback(invalidationCallback)
        _dataSource = dataSource
        return dataSource
    }

    override fun isDisposed(): Boolean = disposed.get()

    private val invalidationCallback = DataSource.InvalidatedCallback() {
        val prevList: PagedList<PhotoWithSize>? = pagedListSubject.value
        val position: Int? = prevList?.lastKey as Int?

        if (!disposed.get()) {
            pagedListSubject.onNext(buildPagedList(position))
        }
    }

    init {
        backgroundThreadExecutor.execute {
            pagedListSubject.onNext(buildPagedList())
        }
    }

    override fun dispose() {
        disposed.set(true)
        _dataSource?.removeInvalidatedCallback(invalidationCallback)
    }

    private fun buildPagedList(initialKey: Int? = null): PagedList<PhotoWithSize> {
        var pagedList: PagedList<PhotoWithSize>

        Log.i("PagedList", "Position: ${initialKey}")

        do {
            val dataSource = newDataSource()
            pagedList = PagedList.Builder<Int, PhotoWithSize>(dataSource, 10)
                    .setBoundaryCallback(callback)
                    .setBackgroundThreadExecutor(backgroundThreadExecutor)
                    .setMainThreadExecutor(mainThreadExecutor)
                    .setInitialKey(initialKey)
                    .build()
        } while(pagedList.isDetached)

        return pagedList
    }

    fun observable() = pagedListSubject.serialize()

    fun refresh() {
        callback.nextPage = 1
        dao.clearPhotos()
    }
}