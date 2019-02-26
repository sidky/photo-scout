package com.github.sidky.data.paging

import android.content.Context
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloClient
import com.github.sidky.photoscout.graphql.InterestingPhotoQuery
import org.koin.standalone.inject

class InterestingFirstPageLoader(
    context: Context,
    params: WorkerParameters): AbstractFirstPageLoader(context, params) {

    private val apolloClient: ApolloClient by inject()

    override suspend fun load(): PhotoLoaderResponse {
        val response = apolloClient.query(InterestingPhotoQuery(1)).execute()
        val photos = response.data()?.interesting()?.photos()?.map { it.fragments().clientPhoto() }
        val next = response.data()?.interesting()?.pagination()?.fragments()?.nextPage()

        return PhotoLoaderResponse(photos, next)
    }
}