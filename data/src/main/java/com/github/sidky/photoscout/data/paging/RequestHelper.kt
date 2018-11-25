package com.github.sidky.photoscout.data.paging

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.github.sidky.photoscout.data.model.*
import com.github.sidky.photoscout.graphql.InterestingQuery
import com.github.sidky.photoscout.graphql.type.PhotoSize as GraphQLPhotoSize

interface RequestCallback {
    fun onSuccess(data: PhotoResponse)
    fun onFailure()
}

abstract class RequestHelper<T> {
    abstract fun initialRequest(client: ApolloClient): ApolloCall<T>
    abstract fun request(client: ApolloClient, page: Int): ApolloCall<T>
    abstract fun convert(data: T?): PhotoResponse

    fun load(client: ApolloClient, page: Int? = null, callback: RequestCallback) {
        val c = if (page == null) {
            initialRequest(client)
        } else {
            request(client, page)
        }
        c.enqueue(object : ApolloCall.Callback<T>() {
            override fun onFailure(e: ApolloException) {
                callback.onFailure()
            }

            override fun onResponse(response: Response<T>) {
                if (response.hasErrors()) {
                    callback.onFailure()
                } else {
                    val data = convert(response.data())
                    callback.onSuccess(data)
                }
            }
        })
    }
}

class InterestingRequestHelper() : RequestHelper<InterestingQuery.Data>() {
    override fun convert(data: InterestingQuery.Data?): PhotoResponse {
        val pagination = data?.interesting()?.pagination()?.let {
            Pagination(it.hasNext(), it.next())
        } ?: Pagination(false, null)

        val photos = data?.interesting()?.photos()?.map {
            val urls = it.photoUrls()?.map {
                PhotoURL(ApolloRequestHelper.size(it.size()), it.url(), it.width(), it.height())
            } ?: emptyList()
            val location = it.location()?.let {
                Location(it.latitude(), it.longitude(), it.accuracy())
            }
            Photo(it.id(), it.ownerName(), location, urls)
        } ?: emptyList()

        return PhotoResponse(photos, pagination)
    }

    override fun initialRequest(client: ApolloClient): ApolloCall<InterestingQuery.Data> {
        val query = InterestingQuery.builder().build()
        return client.query(query)
    }

    override fun request(client: ApolloClient, page: Int): ApolloCall<InterestingQuery.Data> {
        val query = InterestingQuery.builder().page(page).build()
        return client.query(query)
    }
}

data class BoundingBox(val minLongitude: Double, val minLatitude: Double, val maxLongitude: Double, val maxLatitude: Double)

object ApolloRequestHelper {
    fun size(s: GraphQLPhotoSize): PhotoSize {
        return when (s) {
            GraphQLPhotoSize.THUMBNAIL -> PhotoSize.THUMBNAIL
            GraphQLPhotoSize.SMALL -> PhotoSize.SMALL
            GraphQLPhotoSize.SMALL320 -> PhotoSize.SMALL_320
            GraphQLPhotoSize.SQUARE -> PhotoSize.SQUARE
            GraphQLPhotoSize.LARGESQUARE -> PhotoSize.LARGE_SQUARE
            GraphQLPhotoSize.MEDIUM -> PhotoSize.MEDIUM
            GraphQLPhotoSize.MEDIUM640 -> PhotoSize.MEDIUM_640
            GraphQLPhotoSize.MEDIUM800 -> PhotoSize.MEDIUM_800
            GraphQLPhotoSize.LARGE -> PhotoSize.LARGE
            GraphQLPhotoSize.ORIGINAL -> PhotoSize.ORIGINAL
            else -> PhotoSize.UNKNOWN
        }
    }
}

