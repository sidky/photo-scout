package com.github.sidky.data.converter

import com.github.sidky.common.Converter
import com.github.sidky.common.ListConverter
import com.github.sidky.data.dao.Location
import com.github.sidky.data.dao.Photo
import com.github.sidky.data.dao.PhotoURL
import com.github.sidky.data.dao.PhotoWithURL
import com.github.sidky.photoscout.graphql.fragment.ClientPhoto

class ApolloLocationToDBConverter : Converter<Location, ClientPhoto.Location> {
    override fun convert(t: ClientPhoto.Location): Location =
            Location(t.latitude(), t.longitude(), t.accuracy())
}

class ApolloPhotoUrlToDBConverter : Converter<PhotoURL, ClientPhoto.PhotoUrl> {
    override fun convert(t: ClientPhoto.PhotoUrl): PhotoURL {
        return PhotoURL(id = 0, photoId = 0, url = t.url(), width = t.width(), height = t.height())
    }
}

class PhotoUrlListConverter(converter: ApolloPhotoUrlToDBConverter):
    ListConverter<PhotoURL, ClientPhoto.PhotoUrl>(converter)

class ApolloPhotoToDBPhotoConverter(private val locationConverter: ApolloLocationToDBConverter) : Converter<Photo, ClientPhoto> {
    override fun convert(t: ClientPhoto): Photo {
        return Photo(
            id = 0,
            photoId = t.id(),
            owner = t.ownerName(),
            location = locationConverter.convertNullable(t.location()))
    }
}

class ApolloPhotoToDBPhotoWithURLConverter(
    private val photoConverter: ApolloPhotoToDBPhotoConverter,
    private val urlConverter: PhotoUrlListConverter): Converter<PhotoWithURL, ClientPhoto> {

    override fun convert(t: ClientPhoto): PhotoWithURL {
        val urls = urlConverter.convertNullable(t.photoUrls())
        val photo = photoConverter.convert(t)
        return PhotoWithURL(photo, urls ?: emptyList())
    }
}

class ApolloPhotoListToDBConverter(converter: ApolloPhotoToDBPhotoWithURLConverter):
    ListConverter<PhotoWithURL, ClientPhoto>(converter)
