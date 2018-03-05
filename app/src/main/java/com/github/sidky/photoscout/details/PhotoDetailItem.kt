package com.github.sidky.photoscout.details

import com.github.sidky.photoscout.api.flickr.FlickrTag
import com.github.sidky.photoscout.api.flickr.PhotoInfo

sealed class PhotoDetailItem {
    data class Title(val title: String) : PhotoDetailItem()
    data class Owner(val ownerName: String): PhotoDetailItem()
    data class Description(val description: String): PhotoDetailItem()
    data class Location(val latitude: Double, val longitude: Double): PhotoDetailItem()
    data class Tags(val tags: List<FlickrTag>): PhotoDetailItem()

    companion object {
        fun generateItems(photoInfo: PhotoInfo): List<PhotoDetailItem> {
            val items = mutableListOf<PhotoDetailItem>()

            items.add(Title(photoInfo.title.content))
            items.add(Owner(photoInfo.owner.realname))
            items.add(Description(photoInfo.description.content))
            photoInfo.location?.let {
                items.add(Location(latitude = it.latitude, longitude = it.longitude))
            }
//            photoInfo.tag?.tag?.let {
//                items.add(Tags(it))
//            }

            return items
        }
    }
}