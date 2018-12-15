package com.github.sidky.photoscout.maps

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class PhotoClusterRenderer(val context: Context,
                           googleMap: GoogleMap,
                           val clusterManager: ClusterManager<PhotoClusterItem>,
                           val backgroundScope: CoroutineDispatcher,
                           val foregroundScope: CoroutineDispatcher) :
    DefaultClusterRenderer<PhotoClusterItem>(context, googleMap, clusterManager), CoroutineScope {
    override val coroutineContext: CoroutineContext = backgroundScope

    private val iconGenerator: IconGenerator
    private val imageView: ImageView

    init {
        iconGenerator = IconGenerator(context)
        imageView = ImageView(context)
        imageView.layoutParams = ViewGroup.LayoutParams(40, 40)

        iconGenerator.setContentView(imageView)
    }

    override fun onBeforeClusterItemRendered(item: PhotoClusterItem?, markerOptions: MarkerOptions?) {
        Timber.e(item?.url)
        Glide.with(context).asBitmap().load(item?.url).into(MarkerTarget(context, markerOptions!!, clusterManager))
//        runBlocking {
//            val icon = async(backgroundScope) {
//                val bitmap = Glide.with(context).asBitmap().load(item?.url).submit(40, 40).get()
//                imageView.setImageBitmap(bitmap)
//                iconGenerator.setContentView(imageView)
//                BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())
//            }.await()
//
//            withContext(foregroundScope) {
//                markerOptions?.icon(icon)
//            }
//        }
    }
}