package com.github.sidky.data.operation

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloClient
import com.github.sidky.data.paging.execute
import com.github.sidky.photoscout.graphql.BookmarkPhotoMutation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class BookmarkPhoto(val context: Context, parameters: WorkerParameters) : Worker(context, parameters), KoinComponent {

    private val apolloClient: ApolloClient by inject()

    override fun doWork(): Result {
        val id = inputData.getString("id")

        return if (id != null) {
            val op = BookmarkPhotoMutation(id)

            runBlocking {
                withContext(Dispatchers.IO) {
                    val resp = apolloClient.mutate(op).execute()

                    if (resp.hasErrors() || resp.data()?.bookmarkPhoto()?.success() != true) {
                        Result.failure()
                    } else {
                        Result.success()
                    }
                }
            }
        } else {
            Timber.e("BookmarkPhoto called with null id")
            Result.failure()
        }
    }

}