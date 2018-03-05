package com.github.sidky.photoscout.api.flickr

import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class BoundingBox(val minLongitude: Double,
                       val minLatitude: Double,
                       val maxLongitude: Double,
                       val maxLatitude: Double) {
    override fun toString(): String = "${minLongitude},${minLatitude},${maxLongitude},${maxLatitude}"
}

interface FlickrService {
    @GET("?method=flickr.interestingness.getList&extras=geo,url_t,url_sq,url_s,url_q,url_m,url_n,url_z,url_c,url_l,url_o")
    fun interesting(@Query("page") page: Int? = null): Call<PhotoListResponse>

    @GET("?method=flickr.photos.search&sort=interestingness-desc&extras=geot&extras=geo,url_t,url_sq,url_s,url_q,url_m,url_n,url_z,url_c,url_l,url_o")
    fun search(@Query("text") text: String? = null,
               @Query("bbox") boundingBox: BoundingBox? = null,
               @Query("accuracy") accuracy: Int? = null,
               @Query("page") page: Int? = null): Call<PhotoListResponse>

    @GET("?method=flickr.photos.getInfo")
    fun info(@Query("photo_id") photoId: Long): Call<PhotoInfoResponse>
}