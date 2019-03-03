package com.github.sidky.photoscout.map

import com.github.sidky.data.dao.ThumbnailWithLocation
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import timber.log.Timber

class ThumbnailItem(private val thumbnail: ThumbnailWithLocation) : ClusterItem {

    init {
        Timber.d("Thumbnail: ${thumbnail}")
    }
    override fun getSnippet(): String = ""

    override fun getTitle(): String = ""

    override fun getPosition(): LatLng = LatLng(thumbnail.location.latitude, thumbnail.location.longitude)

    fun id() = thumbnail.id

    fun width() = thumbnail.width

    fun height() = thumbnail.height

    fun url() = thumbnail.url
}