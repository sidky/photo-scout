package com.github.sidky.photoscout.api.flickr

import com.squareup.moshi.Json

data class PhotoListResponse(val photos: Photos, val stat: String)

data class Photos(val page: Int, val pages: Int, val perpage: Int, val total: Int, val photo: List<Photo>)

data class Photo(val id: Long,
                 val owner: String,
                 val secret: String,
                 val server: Int,
                 val farm: Int,
                 val title: String,
                 val latitude: Double?,
                 val longitude: Double?,
                 val accuracy: Double?,

                 @Json(name = "url_sq") val squareUrl: String?,
                 @Json(name = "height_sq") val lHeight: Int?,
                 @Json(name = "width_sq") val lWidth: Int?,

                 @Json(name = "url_q") val largeSquareUrl: String?,
                 @Json(name = "height_q") val largeSquareHeight: Int?,
                 @Json(name = "width_q") val largeSquareWidth: Int?,

                 @Json(name = "url_t") val thumbnailUrl: String?,
                 @Json(name = "height_t") val thumbnailHeight: Int?,
                 @Json(name = "width_t") val thumbnailWidth: Int?,

                 @Json(name = "url_s") val smallUrl: String?,
                 @Json(name = "height_s") val smallHeight: Int?,
                 @Json(name = "width_s") val smallWidth: Int?,

                 @Json(name = "url_n") val small320Url: String?,
                 @Json(name = "height_n") val small320Height: Int?,
                 @Json(name = "width_n") val small320Width: Int?,

                 @Json(name = "url_m") val mediumUrl: String?,
                 @Json(name = "height_m") val mediumHeight: Int?,
                 @Json(name = "width_m") val mediumWidth: Int?,

                 @Json(name = "url_z") val medium640Url: String?,
                 @Json(name = "height_z") val medium640Height: Int?,
                 @Json(name = "width_z") val medium640Width: Int?,

                 @Json(name = "url_c") val medium800Url: String?,
                 @Json(name = "height_c") val medium800Height: Int?,
                 @Json(name = "width_c") val medium800Width: Int?,

                 @Json(name = "url_l") val largeUrl: String?,
                 @Json(name = "height_l") val largeHeight: Int?,
                 @Json(name = "width_l") val largeWidth: Int?,

                 @Json(name = "url_o") val originalUrl: String?,
                 @Json(name = "height_o") val originalHeight: Int?,
                 @Json(name = "width_o") val originalWidth: Int?)