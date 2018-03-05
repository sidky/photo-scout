package com.github.sidky.photoscout.data.converter

import com.github.sidky.photoscout.data.entity.Location
import com.github.sidky.photoscout.data.entity.Photo
import com.github.sidky.photoscout.data.entity.PhotoSize
import com.github.sidky.photoscout.api.flickr.Photo as FlickrPhoto
import com.github.sidky.photoscout.api.flickr.PhotoSize as FlickrPhotoSize

object FlickrPhotoListConverter {
    fun convert(list: List<FlickrPhoto>, pageSize: Int, page: Int): List<Photo> =
        list.mapIndexed { index, photo -> FlickrPhotoConverter.convert(photo = photo, order = pageSize * page + index) }
}

object FlickrPhotoSizeListConverter {
    fun convert(list: List<FlickrPhoto>): List<PhotoSize> =
            list.map(FlickrPhotoSizeConverter::convert).flatten()
}

object FlickrPhotoConverter {
    fun convert(photo: FlickrPhoto, order: Int): Photo =
            Photo(id = photo.id, order = order, title = photo.title, location = location(photo))

    private fun location(photo: FlickrPhoto): Location? {
        val latitude = photo.latitude
        val longitude = photo.longitude
        val accuracy = photo.accuracy

        return if (latitude != null && longitude != null && accuracy != null) {
            Location(latitude, longitude, accuracy)
        } else {
            null
        }
    }
}

object FlickrPhotoSizeConverter {
    fun convert(photo: FlickrPhoto): List<PhotoSize> {
        return with(photo) { arrayOf(
                thumbnail,
                small,
                small320,
                square,
                medium,
                medium640,
                medium800,
                large,
                largeSquare,
                original
        )}.filterNotNull().map { convert(photo, it) }
    }

    private fun convert(photo: FlickrPhoto, photoSize: FlickrPhotoSize): PhotoSize =
            PhotoSize(photoId = photo.id,
                    url = photoSize.url,
                    width = photoSize.width,
                    height = photoSize.height)
}