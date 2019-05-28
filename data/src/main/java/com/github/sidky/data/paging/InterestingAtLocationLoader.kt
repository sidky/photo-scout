package com.github.sidky.data.paging

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.work.Data
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.github.sidky.photoscout.graphql.SearchPhotoQuery
import org.koin.standalone.inject
import timber.log.Timber
import com.github.sidky.photoscout.graphql.type.BoundingBox as GraphQLBoundingBox

data class BoundingBox(
    val minLongitude: Double,
    val minLatitude: Double,
    val maxLongitude: Double,
    val maxLatitude: Double) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble())

    fun toGraphQLBoundingBox(): GraphQLBoundingBox {
        return GraphQLBoundingBox
            .builder()
            .minLatitude(minLatitude)
            .minLongitude(minLongitude)
            .maxLatitude(maxLatitude)
            .maxLongitude(maxLongitude)
            .build()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(minLongitude)
        parcel.writeDouble(minLatitude)
        parcel.writeDouble(maxLongitude)
        parcel.writeDouble(maxLatitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        return if (other is BoundingBox) {
            withinRange(minLongitude, other.minLongitude) &&
                    withinRange(minLatitude, other.minLatitude) &&
                    withinRange(maxLongitude, other.maxLongitude) &&
                    withinRange(maxLatitude, other.maxLatitude)
        } else {
            false
        }
    }

    private fun withinRange(p: Double, q: Double) = Math.abs(p - q) < 1e-4

    companion object CREATOR : Parcelable.Creator<BoundingBox> {
        override fun createFromParcel(parcel: Parcel): BoundingBox {
            return BoundingBox(parcel)
        }

        override fun newArray(size: Int): Array<BoundingBox?> {
            return arrayOfNulls(size)
        }
    }
}

object InterestingAtLocationArgUtil {
    fun fromData(data: Data): BoundingBox {
        val minLongitude = data.getDouble(ARG_MIN_LONGITUDE, 0.0)
        val minLatitude = data.getDouble(ARG_MIN_LATITUDE, 0.0)
        val maxLongitude = data.getDouble(ARG_MAX_LONGITUDE, 0.0)
        val maxLatitude = data.getDouble(ARG_MAX_LATITUDE, 0.0)

        return BoundingBox(minLongitude, minLatitude, maxLongitude, maxLatitude)
    }

    fun toDataBuilder(location: BoundingBox): Data.Builder {
        return Data.Builder()
            .putDouble(ARG_MIN_LONGITUDE, location.minLongitude)
            .putDouble(ARG_MIN_LATITUDE, location.minLatitude)
            .putDouble(ARG_MAX_LONGITUDE, location.maxLongitude)
            .putDouble(ARG_MAX_LATITUDE, location.maxLatitude)
    }

    private const val ARG_MIN_LONGITUDE = "minLongitude"
    private const val ARG_MIN_LATITUDE = "minLatitude"
    private const val ARG_MAX_LONGITUDE = "maxLongitude"
    private const val ARG_MAX_LATITUDE = "maxLatitude"
}

class InterestingAtLocationFirstPageLoader(context: Context, params: WorkerParameters) : AbstractFirstPageLoader(context, params) {

    private val apolloClient: ApolloClient by inject()

    override suspend fun load(): GraphQLResponse {
        val data = InterestingAtLocationArgUtil.fromData(inputData)
        Timber.d("Data: ${inputData}")
        Timber.d("Query: ${data}")
        val response = apolloClient.query(SearchPhotoQuery(Input.absent(), Input.fromNullable(data.toGraphQLBoundingBox()), 1)).execute()
        return if (response.hasErrors()) {
            GraphQLResponse.Failure()
        } else {
            val photos = response.data()?.search()?.photos()?.map { it.fragments().clientPhoto() }
            val next = response.data()?.search()?.pagination()?.fragments()?.nextPage()

            return GraphQLResponse.Success(PhotoLoaderResponse(photos, next))
        }
    }
}

class InterestingAtLocationNextPageLoader(
    context: Context,
    params: WorkerParameters
): AbstractNextPageLoader(context, params) {
    private val apolloClient: ApolloClient by inject()

    override suspend fun load(page: Int): GraphQLResponse {
        val data = InterestingAtLocationArgUtil.fromData(inputData)
        Timber.d("Data: ${inputData}")
        Timber.d("Query: ${data}")
        val resp = apolloClient.query(SearchPhotoQuery(Input.absent(), Input.fromNullable(data.toGraphQLBoundingBox()), page)).execute()
        return if (resp.hasErrors()) {
            GraphQLResponse.Failure()
        } else {
            val photos = resp.data()?.search()?.photos()?.map { it.fragments().clientPhoto() }
            val next = resp.data()?.search()?.pagination()?.fragments()?.nextPage()

            return GraphQLResponse.Success(PhotoLoaderResponse(photos, next))
        }
    }
}