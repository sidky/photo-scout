package com.github.sidky.photoscout.maps

import com.github.sidky.photoscout.data.PhotoWithURL
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class PhotoClusterItem(photo: PhotoWithURL) : ClusterItem {
    val latLng: LatLng
    val url: String?

    init {
        latLng = photo.photo.location?.let {
            LatLng(it.latitude, it.longitude)
        } ?: LatLng(0.0, 0.0)

        url = photo.urls.minBy { it.width * it.height }?.url
    }
    override fun getSnippet(): String = ""

    override fun getTitle(): String = ""

    override fun getPosition(): LatLng = latLng

}