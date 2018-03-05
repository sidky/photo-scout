package com.github.sidky.photoscout.api.flickr

import com.squareup.moshi.Json

data class PhotoListResponse(val photos: Photos, val stat: String)

data class Photos(val page: Int, val pages: Int, val perpage: Int, val total: Int, val photo: List<Photo>)

enum class SizeType {
    SQUARE,
    LARGE_SQUARE,
    THUMBNAIL,
    SMALL,
    SMALL_320,
    MEDIUM,
    MEDIUM_640,
    MEDIUM_800,
    LARGE, ORIGINAL
}

data class PhotoSize(val type: SizeType, val width: Int, val height: Int, val url: String)

data class Photo(val id: Long,
                 val owner: String,
                 val secret: String,
                 val server: Int,
                 val farm: Int,
                 val title: String,
                 val latitude: Double?,
                 val longitude: Double?,
                 val accuracy: Int?,

                 @Json(name = "url_sq") val squareUrl: String?,
                 @Json(name = "height_sq") val squareHeight: Int?,
                 @Json(name = "width_sq") val squareWidth: Int?,

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
                 @Json(name = "width_o") val originalWidth: Int?) {

    val square: PhotoSize?
        get() = photoSize(SizeType.SQUARE, squareWidth, squareHeight, squareUrl)

    val largeSquare: PhotoSize?
        get() = photoSize(SizeType.LARGE_SQUARE, largeWidth, largeSquareHeight, largeSquareUrl)

    val thumbnail: PhotoSize?
        get() = photoSize(SizeType.THUMBNAIL,
                thumbnailWidth,
                thumbnailHeight,
                thumbnailUrl)

    val small: PhotoSize?
        get() = photoSize(SizeType.SMALL,
                smallWidth,
                smallHeight,
                smallUrl)

    val small320: PhotoSize?
        get() = photoSize(SizeType.SMALL_320,
                small320Width,
                small320Height,
                small320Url)

    val medium: PhotoSize?
        get() = photoSize(SizeType.MEDIUM,
                mediumWidth,
                mediumHeight,
                mediumUrl)

    val medium640: PhotoSize?
        get() = photoSize(SizeType.MEDIUM_640,
                medium640Width,
                medium640Height,
                medium640Url)

    val medium800: PhotoSize?
        get() = photoSize(SizeType.MEDIUM_800,
                medium800Width,
                medium800Height,
                medium800Url)

    val large: PhotoSize?
        get() = photoSize(SizeType.LARGE,
                largeWidth,
                largeHeight,
                largeUrl)

    val original: PhotoSize?
        get() = photoSize(SizeType.ORIGINAL,
                originalWidth,
                originalHeight,
                originalUrl)






    private fun photoSize(sizeType: SizeType,
                          width: Int?,
                          height: Int?,
                          url: String?): PhotoSize? {
        return if (width != null && height != null && url != null) {
            PhotoSize(sizeType, width, height, url)
        } else {
            null
        }
    }
}

data class PhotoOwner(val nsid: String,
                      val username: String,
                      val realname: String,
                      val location: String)

data class StringContent(@Json(name="_content") val content: String)

data class FlickrTag(val id: String,
                     val author: String,
                     val authorName: String,
                     val raw: String,
                     @Json(name="_content") val content: String,
                     @Json(name = "machine_tag") val machineTag: Int)

data class FlickrTagList(val tag: List<FlickrTag>)

data class FlickrLocation(val latitude: Double, val longitude: Double, val accuracy: Int, val context: Int)

data class PhotoInfo(val id: Long,
                     val secret: String,
                     val server: String,
                     val farm: Int,
                     @Json(name="dateuploaded") val dateUploaded: Long,
                     val owner: PhotoOwner,
                     val title: StringContent,
                     val description: StringContent,
                     val tag: FlickrTagList?,
                     val location: FlickrLocation?)

data class PhotoInfoResponse(
        val photo: PhotoInfo,
        val stat: String)