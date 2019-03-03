package com.github.sidky.data.converter

import com.github.sidky.common.Converter
import com.github.sidky.common.ListConverter
import com.github.sidky.data.repository.*
import com.github.sidky.photoscout.graphql.PhotoDetailQuery

class ApolloOwnerToModelConverter : Converter<Owner, PhotoDetailQuery.Owner> {
    override fun convert(t: PhotoDetailQuery.Owner): Owner =
            Owner(t.name(), t.location())
}

class ApolloTagToModelConverter : Converter<Tag, PhotoDetailQuery.Tag> {
    override fun convert(t: PhotoDetailQuery.Tag): Tag =
            Tag(t.raw(), t.isMachineTag)
}

class ApolloListTagToModelConverter(conv : ApolloTagToModelConverter) : ListConverter<Tag, PhotoDetailQuery.Tag>(conv)

class ApolloExifToModelConverter : Converter<Exif, PhotoDetailQuery.Exif> {
    override fun convert(t: PhotoDetailQuery.Exif): Exif =
            Exif(t.label(), t.raw())
}

class ApolloLocationToModelConverter : Converter<Location, PhotoDetailQuery.Location> {
    override fun convert(t: PhotoDetailQuery.Location): Location =
            Location(t.latitude(), t.longitude(), t.accuracy())
}

class ApolloListExifToModelConverter(conv : ApolloExifToModelConverter) : ListConverter<Exif, PhotoDetailQuery.Exif>(conv)

class ApolloDetailToModelConverter(
    private val ownerConverter: ApolloOwnerToModelConverter,
    private val tagConverter: ApolloListTagToModelConverter,
    private val exifConverter: ApolloListExifToModelConverter,
    private val locationConverter : ApolloLocationToModelConverter
) : Converter<PhotoDetail, PhotoDetailQuery.Detail> {
    override fun convert(t: PhotoDetailQuery.Detail): PhotoDetail  =
            PhotoDetail(
                t.id(),
                t.uploadedAt(),
                ownerConverter.convert(t.owner()),
                t.title(),
                t.description(),
                t.camera(),
                tagConverter.convertNullable(t.tags()) ?: emptyList(),
                exifConverter.convertNullable(t.exif()) ?: emptyList(),
                locationConverter.convertNullable(t.location()))
}