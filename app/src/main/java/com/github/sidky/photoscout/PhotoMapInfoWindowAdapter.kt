package com.github.sidky.photoscout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.github.sidky.photoscout.data.PhotoWithURL
import com.github.sidky.photoscout.databinding.PhotoMarkerBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class PhotoMapInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    val layoutInflater: LayoutInflater by lazy {
        LayoutInflater.from(context)
    }

    override fun getInfoContents(marker: Marker?): View? {
        val tag = marker?.tag
        return when (tag) {
            is PhotoWithURL -> {
                val markerBinding: PhotoMarkerBinding = DataBindingUtil.inflate(layoutInflater, R.layout.photo_marker, null, false)
                val url = thumbnail(tag)
                if (url != null) {
                    Glide.with(context).load(url).into(markerBinding.thumbnail)
                }
                markerBinding.root
            }
            else -> null
        }

    }

    override fun getInfoWindow(marker: Marker?): View? = null

    private fun thumbnail(photoWithURL: PhotoWithURL): String? {
        return photoWithURL.urls.minBy { it.width * it.height }?.url
    }
}