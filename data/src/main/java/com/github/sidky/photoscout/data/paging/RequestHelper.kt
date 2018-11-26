package com.github.sidky.photoscout.data.paging

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.github.sidky.photoscout.data.model.*
import com.github.sidky.photoscout.graphql.InterestingQuery
import com.github.sidky.photoscout.graphql.SearchQuery
import com.github.sidky.photoscout.graphql.fragment.PaginationProp
import com.github.sidky.photoscout.graphql.fragment.PhotoProp
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

abstract class BasePhotoListHelper<T> : RequestHelper<T>() {
    override fun convert(data: T?): PhotoResponse {
        val pagination = getPagination(data)?.let {
            Pagination(it.hasNext(), it.next())
        } ?: Pagination(false, null)

        val photos = getPhotoList(data)?.map {
            val urls = it.photoUrls()?.map {
                PhotoURL(ApolloRequestHelper.size(it.size()), it.url(), it.width(), it.height())
            } ?: emptyList()
            val location = it.location()?.let {
                Location(it.latitude(), it.longitude(), it.accuracy())
            }
            Photo(it.id(), it.ownerName(), location, urls)
        }

        return PhotoResponse(photos, pagination)
    }

    abstract fun getPagination(data: T?): PaginationProp?
    abstract fun getPhotoList(data: T?): List<PhotoProp>
}

class InterestingRequestHelper() : BasePhotoListHelper<InterestingQuery.Data>() {
    override fun getPagination(data: InterestingQuery.Data?): PaginationProp? {
        return data?.interesting()?.pagination()?.fragments()?.paginationProp()
    }

    override fun getPhotoList(data: InterestingQuery.Data?): List<PhotoProp> {
        return data?.interesting()?.photos()?.map {
            it.fragments()?.photoProp()
        } ?: emptyList()
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

class SearchRequestHelper(private val query: String) : BasePhotoListHelper<SearchQuery.Data>() {
    override fun initialRequest(client: ApolloClient): ApolloCall<SearchQuery.Data> {
        val query = SearchQuery.builder().query(query).build()
        return client.query(query)
    }

    override fun request(client: ApolloClient, page: Int): ApolloCall<SearchQuery.Data> {
        val query = SearchQuery.builder().query(query).page(page).build()
        return client.query(query)
    }

    override fun getPagination(data: SearchQuery.Data?): PaginationProp? {
        return data?.search()?.pagination()?.fragments()?.paginationProp()
    }

    override fun getPhotoList(data: SearchQuery.Data?): List<PhotoProp> {
        return data?.search()?.photos()?.map {
            it?.fragments()?.photoProp()
        }?.filterNotNull() ?: emptyList()
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

