package com.github.sidky.photoscout.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.github.sidky.photoscout.data.model.Exif
import com.github.sidky.photoscout.data.model.Location
import com.github.sidky.photoscout.data.model.PhotoDetail
import com.github.sidky.photoscout.data.paging.InterestingRequestHelper
import com.github.sidky.photoscout.data.paging.PhotoBoundaryCallback
import com.github.sidky.photoscout.data.paging.SearchRequestHelper
import com.github.sidky.photoscout.graphql.PhotoDetailQuery
import timber.log.Timber

data class Listing(val pagedList: LiveData<PagedList<PhotoWithURL>>)

class PhotoRepository(private val apolloClient: ApolloClient, private val dao: PhotoDAO) {

    val pagedListConfig = PagedList.Config.Builder()
        .setEnablePlaceholders(true)
        .setPageSize(10)
        .build()

    var maxWidth: Int = 300
    var maxHeight: Int = 300

    fun setMaxDimension(dimension: Int) {
        maxWidth = dimension
        maxHeight = dimension
    }

    private val comparator = Comparator<SizedURL> { o1, o2 ->
        if (o1.width == o2.width) {
            o1.height - o2.height
        } else {
            o1.width - o2.width
        }
    }

    fun loadInteresting(): Listing {
        val boundaryCallback = PhotoBoundaryCallback<PhotoWithURL>(apolloClient, dao, InterestingRequestHelper())

        Timber.e("Inside repository")

        return Listing(toLiveData(dao.photos(), boundaryCallback))
    }

    fun search(query: String): Listing {
        val boundaryCallback = PhotoBoundaryCallback<PhotoWithURL>(apolloClient, dao, SearchRequestHelper(query))
        return Listing(toLiveData(dao.photos(), boundaryCallback))
    }

    fun getDetails(photoId: String): LiveData<PhotoDetail?> {
        val query = PhotoDetailQuery.builder().photoId(photoId).build()

        val data = MutableLiveData<PhotoDetail?>()

        apolloClient.query(query).enqueue(object : ApolloCall.Callback<PhotoDetailQuery.Data>() {
            override fun onFailure(e: ApolloException) {
                Timber.e(e)
                data.postValue(null)
            }

            override fun onResponse(response: Response<PhotoDetailQuery.Data>) {
                val responseData = response.data()?.detail()
                val detail = responseData?.let {
                    val tags = responseData.tags()?.flatMap {
                        if (!it.isMachineTag) {
                            listOf(it.raw())
                        } else {
                            emptyList()
                        }
                    } ?: emptyList()
                    val exif = responseData.exif()?.map {
                        Exif(label = it.label(), value = it.raw())
                    } ?: emptyList()

                    val location = responseData.location()?.let {
                        Location(latitude = it.latitude(), longitude = it.longitude(), accuracy = it.accuracy())
                    }

                    val converted = PhotoDetail(
                        photoId = responseData.id(),
                        ownerName = responseData.owner().name(),
                        ownerLocation = responseData.owner().location(),
                        title = responseData.title(),
                        description = responseData.description(),
                        camera = responseData.camera(),
                        tags = tags,
                        exif = exif,
                        location = location
                    )

                    data.postValue(converted)
                }
            }
        })

        return data
    }

    private fun <T>toLiveData(factory: DataSource.Factory<Int, T>, boundaryCallback: PhotoBoundaryCallback<T>): LiveData<PagedList<T>> {
        return LivePagedListBuilder(factory, pagedListConfig)
            .setBoundaryCallback(boundaryCallback)
            .build()
    }
}