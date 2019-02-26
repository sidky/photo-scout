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

abstract class AbstractNextPageLoader(
    context: Context,
    params: WorkerParameters
): Worker(context, params), KoinComponent {
    private val converter: ApolloPhotoListToDBConverter by inject()
    private val dao: PhotoDAO by inject()
    private val loadingState: LoadingState by inject()

    abstract suspend fun load(page: Int): PhotoLoaderResponse

    override fun doWork(): Result {
        val page = inputData.getInt("page", 1)

        return runBlocking {
            val response = load(page)
            val dbPhotos = converter.convertNullable(response.photos)

            val pagination = response.next

            loadingState.hasNext = pagination?.hasNext() ?: false
            loadingState.next = pagination?.next() ?: 0

            if (dbPhotos != null) {
                dao.insertPhotoWithURLS(dbPhotos)
            }

            Result.success(WorkResultUtil.success(pagination))
        }
    }

}