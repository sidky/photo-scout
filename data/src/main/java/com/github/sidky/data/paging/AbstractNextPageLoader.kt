package com.github.sidky.data.paging

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.sidky.data.apollo.TokenProvider
import com.github.sidky.data.converter.ApolloPhotoListToDBConverter
import com.github.sidky.data.converter.WorkResultUtil
import com.github.sidky.data.dao.LoadingState
import com.github.sidky.data.dao.PhotoDAO
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

abstract class AbstractNextPageLoader(
    context: Context,
    params: WorkerParameters
): Worker(context, params), KoinComponent {
    private val converter: ApolloPhotoListToDBConverter by inject()
    private val dao: PhotoDAO by inject()
    private val loadingState: LoadingState by inject()
    private val tokenProvider: TokenProvider by inject()

    abstract suspend fun load(page: Int): GraphQLResponse

    override fun doWork(): Result {
        val page = inputData.getInt("page", 1)

        return runBlocking {
            val response = load(page)
            when (response) {
                is GraphQLResponse.Success -> {
                    val resp = response.response
                    val dbPhotos = converter.convertNullable(resp.photos)

                    val pagination = resp.next

                    loadingState.hasNext = pagination?.hasNext() ?: false
                    loadingState.next = pagination?.next() ?: 0

                    Timber.tag("PHOTO").i("Finished ${this@AbstractNextPageLoader.javaClass.name}")
                    if (dbPhotos != null) {
                        dao.insertPhotoWithURLS(dbPhotos)
                    }
                    Result.success(WorkResultUtil.success(pagination))
                }
                is GraphQLResponse.Failure -> {
                    refreshToken()
                    Result.retry()
                }
            }
        }
    }

    suspend fun refreshToken() {
        tokenProvider.refresh()
    }
}