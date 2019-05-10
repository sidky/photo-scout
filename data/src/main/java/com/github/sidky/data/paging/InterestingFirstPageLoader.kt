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

    override suspend fun load(): GraphQLResponse {
        val response = apolloClient.query(InterestingPhotoQuery(1)).execute()
        return if (response.hasErrors()) {
            GraphQLResponse.Failure()
        } else {
            val photos = response.data()?.interesting()?.photos()?.map { it.fragments().clientPhoto() }
            val next = response.data()?.interesting()?.pagination()?.fragments()?.nextPage()
            GraphQLResponse.Success(PhotoLoaderResponse(photos, next))
        }
    }
}