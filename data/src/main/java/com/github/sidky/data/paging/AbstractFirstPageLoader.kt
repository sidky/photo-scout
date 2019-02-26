package com.github.sidky.data.paging

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.sidky.data.converter.ApolloPhotoListToDBConverter
import com.github.sidky.data.converter.WorkResultUtil
import com.github.sidky.data.dao.LoadingState
import com.github.sidky.data.dao.PhotoDAO
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

abstract class AbstractFirstPageLoader(
    context: Context,
    params: WorkerParameters
): Worker(context, params), KoinComponent {
    private val converter: ApolloPhotoListToDBConverter by inject()
    private val dao: PhotoDAO by inject()
    private val loadingState: LoadingState by inject()

    abstract suspend fun load(): PhotoLoaderResponse

    override fun doWork(): Result {
        return runBlocking {
            Timber.e("BEGIN")
            val response = load()
            val dbPhotos = converter.convertNullable(response.photos)

            Timber.e("LOADED: ${dbPhotos}")

            val pagination = response.next

            loadingState.hasNext = pagination?.hasNext() ?: false
            loadingState.next = pagination?.next() ?: 0

            if (dbPhotos != null) {
                dao.setPhotsWithURLs(dbPhotos)
            } else {
                dao.deleteAll()
            }
            Result.success(WorkResultUtil.success(pagination))
        }
    }
}