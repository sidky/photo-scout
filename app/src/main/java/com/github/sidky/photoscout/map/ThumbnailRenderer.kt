package com.github.sidky.photoscout.map

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import timber.log.Timber
import com.bumptech.glide.request.target.Target as GlideTarget

class ThumbnailRenderer(private val context: Context, googleMap: GoogleMap, private val clusterManager: ClusterManager<ThumbnailItem>) :
    DefaultClusterRenderer<ThumbnailItem>(context, googleMap, clusterManager) {
    private val singleThumbnail: ImageView
    private val iconGenerator = IconGenerator(context)

    init {
        singleThumbnail = ImageView(context)
        iconGenerator.setContentView(singleThumbnail)
    }

    override fun onBeforeClusterItemRendered(item: ThumbnailItem?, markerOptions: MarkerOptions?) {
        singleThumbnail.layoutParams = ViewGroup.LayoutParams(item?.width() ?: 0, item?.height() ?: 0)
        Timber.d("URL: ${item} ${item?.url()}")
        Glide.with(context).asBitmap()
            .load(item?.url()).into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    clusterManager.markerCollection.markers.find {
                        it.position == item?.position
                    }?.setIcon(BitmapDescriptorFactory.fromBitmap(resource))
                    Timber.d("item set to: ${item?.id()}")
                }
            })
    }
}