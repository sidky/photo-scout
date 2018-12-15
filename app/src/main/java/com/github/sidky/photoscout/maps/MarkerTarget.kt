package com.github.sidky.photoscout.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ui.IconGenerator
import java.lang.ref.WeakReference

class MarkerTarget(context: Context, val markerOptions: MarkerOptions, val clusterManager: ClusterManager<PhotoClusterItem>): Target<Bitmap> {

    private val iconGenerator = IconGenerator(context)

    private val imageView: ImageView

    init {
        imageView = ImageView(context)
        imageView.layoutParams = ViewGroup.LayoutParams(40, 40)
        iconGenerator.setContentView(imageView)
    }

    private var _request: Request? = null

    override fun getRequest(): Request? = _request

    override fun setRequest(request: Request?) {
        _request = request
    }
    override fun onLoadStarted(placeholder: Drawable?) {
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
    }

    override fun getSize(cb: SizeReadyCallback) {
        cb.onSizeReady(40, 40)
    }

    override fun onStop() {
    }

    override fun removeCallback(cb: SizeReadyCallback) {
    }

    override fun onLoadCleared(placeholder: Drawable?) {
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        imageView.setImageBitmap(resource)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())).title("Foo")
        clusterManager.cluster()
    }

    override fun onStart() {
    }

    override fun onDestroy() {
    }
}
