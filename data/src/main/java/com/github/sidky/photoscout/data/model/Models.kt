package com.github.sidky.photoscout.data.model

data class PhotoResponse(val photos: List<Photo>, val pagination: Pagination)

data class Photo(
    val id: String,
    val ownerName: String,
    val location: Location?,
    val urls: List<PhotoURL>)

data class Location(val latitude: Double, val longitude: Double, val accuracy: Int)

data class PhotoURL(val size: PhotoSize, val url: String, val width: Int, val height: Int)

enum class PhotoSize {
    THUMBNAIL, SMALL, SMALL_320, SQUARE, LARGE_SQUARE, MEDIUM, MEDIUM_640, MEDIUM_800, LARGE, ORIGINAL, UNKNOWN
}

data class Pagination(val hasNext: Boolean, val next: Int?)

data class Exif(val label: String, val value: String)

data class PhotoDetail(
    val photoId: String,
    val ownerName: String,
    val ownerLocation: String?,
    val title: String,
    val description: String,
    val camera: String?,
    val tags: List<String>,
    val exif: List<Exif>,
    val location: Location?)