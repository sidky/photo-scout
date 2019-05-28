package com.github.sidky.data.paging

import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.github.sidky.photoscout.graphql.SearchPhotoQuery
import org.koin.standalone.inject
import timber.log.Timber

data class SearchArg(val query: String, val boundingBox: BoundingBox? = null)

object SearchArgUtil {

    fun fromData(data: Data): SearchArg {
        val query = data.getString("query")
        val keys = data.keyValueMap.keys

        val box = if (keys.containsAll(listOf(ARG_MIN_LATITUDE, ARG_MIN_LONGITUDE, ARG_MAX_LATITUDE, ARG_MAX_LONGITUDE))) {
            boundingBox(data)
        } else {
            null
        }

        return SearchArg(query ?: "", box)
    }

    fun toData(arg: SearchArg): Data {
        val builder = Data.Builder().putString("query", arg.query)

        arg.boundingBox?.let {
            builder.putDouble(SearchArgUtil.ARG_MIN_LATITUDE, it.minLatitude)
                .putDouble(SearchArgUtil.ARG_MIN_LONGITUDE, it.minLongitude)
                .putDouble(SearchArgUtil.ARG_MAX_LATITUDE, it.maxLatitude)
                .putDouble(SearchArgUtil.ARG_MAX_LONGITUDE, it.maxLongitude)
        }

        return builder.build()
    }

    fun query(data: Data): String? = data.getString("query")

    private fun boundingBox(data: Data): BoundingBox {
        val minLongitude = data.getDouble(SearchArgUtil.ARG_MIN_LONGITUDE, 0.0)
        val minLatitude = data.getDouble(SearchArgUtil.ARG_MIN_LATITUDE, 0.0)
        val maxLongitude = data.getDouble(SearchArgUtil.ARG_MAX_LONGITUDE, 0.0)
        val maxLatitude = data.getDouble(SearchArgUtil.ARG_MAX_LATITUDE, 0.0)

        return BoundingBox(minLongitude, minLatitude, maxLongitude, maxLatitude)
    }

    private const val ARG_MIN_LONGITUDE = "minLongitude"
    private const val ARG_MIN_LATITUDE = "minLatitude"
    private const val ARG_MAX_LONGITUDE = "maxLongitude"
    private const val ARG_MAX_LATITUDE = "maxLatitude"
}

class SearchFirstPageLoader(
    context: Context,
    params: WorkerParameters
): AbstractFirstPageLoader(context, params) {

    private val apolloClient: ApolloClient by inject()

    override suspend fun load(): GraphQLResponse {
        Timber.d("Data: ${inputData}")
        val arg = SearchArgUtil.fromData(inputData)
        Timber.d("Query: ${arg.query}")
        val response = apolloClient.query(SearchPhotoQuery(Input.fromNullable(arg.query), Input.fromNullable(arg.boundingBox?.toGraphQLBoundingBox()), 1)).execute()
        return if (response.hasErrors()) {
            GraphQLResponse.Failure()
        } else {
            val photos = response.data()?.search()?.photos()?.map { it.fragments().clientPhoto() }
            val next = response.data()?.search()?.pagination()?.fragments()?.nextPage()

            GraphQLResponse.Success(PhotoLoaderResponse(photos, next))
        }
    }
}

class SearchNextPageLoader(
    context: Context,
    params: WorkerParameters
): AbstractNextPageLoader(context, params) {
    private val apolloClient: ApolloClient by inject()

    override suspend fun load(page: Int): GraphQLResponse {
        val arg = SearchArgUtil.fromData(inputData)
        val resp = apolloClient.query(SearchPhotoQuery(Input.fromNullable(arg.query), Input.fromNullable(arg.boundingBox?.toGraphQLBoundingBox()), page)).execute()
        if (resp.hasErrors()) {
            return GraphQLResponse.Failure()
        } else {
            val photos = resp.data()?.search()?.photos()?.map { it.fragments().clientPhoto() }
            val next = resp.data()?.search()?.pagination()?.fragments()?.nextPage()

            return GraphQLResponse.Success(PhotoLoaderResponse(photos, next))
        }
    }
}