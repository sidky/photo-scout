package com.github.sidky.data.paging

import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.github.sidky.photoscout.graphql.SearchPhotoQuery
import org.koin.standalone.inject
import timber.log.Timber

private object SearchArgUtil {
    fun query(data: Data): String? = data.getString("query")
}

class SearchFirstPageLoader(
    context: Context,
    params: WorkerParameters
): AbstractFirstPageLoader(context, params) {

    private val apolloClient: ApolloClient by inject()

    override suspend fun load(): PhotoLoaderResponse {
        Timber.d("Data: ${inputData}")
        Timber.d("Query: ${SearchArgUtil.query(inputData)}")
        val response = apolloClient.query(SearchPhotoQuery(Input.fromNullable(SearchArgUtil.query(inputData) ?: ""), Input.absent(), 1)).execute()
        val photos = response.data()?.search()?.photos()?.map { it.fragments().clientPhoto() }
        val next = response.data()?.search()?.pagination()?.fragments()?.nextPage()

        return PhotoLoaderResponse(photos, next)
    }
}

class SearchNextPageLoader(
    context: Context,
    params: WorkerParameters
): AbstractNextPageLoader(context, params) {
    private val apolloClient: ApolloClient by inject()

    override suspend fun load(page: Int): PhotoLoaderResponse {
        val resp = apolloClient.query(SearchPhotoQuery(Input.fromNullable(SearchArgUtil.query(inputData) ?: ""), Input.absent(), page)).execute()
        val photos = resp.data()?.search()?.photos()?.map { it.fragments().clientPhoto() }
        val next = resp.data()?.search()?.pagination()?.fragments()?.nextPage()

        return PhotoLoaderResponse(photos, next)
    }
}