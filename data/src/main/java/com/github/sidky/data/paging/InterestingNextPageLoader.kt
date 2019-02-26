package com.github.sidky.data.paging

import android.content.Context
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloClient
import com.github.sidky.photoscout.graphql.InterestingPhotoQuery
import org.koin.standalone.inject

class InterestingNextPageLoader(
    context: Context,
    params: WorkerParameters
): AbstractNextPageLoader(context, params) {
    private val apolloClient: ApolloClient by inject()

    override suspend fun load(page: Int): PhotoLoaderResponse {
        val resp = apolloClient.query(InterestingPhotoQuery(page)).execute()
        val photos = resp.data()?.interesting()?.photos()?.map { it.fragments().clientPhoto() }
        val next = resp.data()?.interesting()?.pagination()?.fragments()?.nextPage()

        return PhotoLoaderResponse(photos, next)
    }
}