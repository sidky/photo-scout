package com.github.sidky.photoscout.map

import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import io.reactivex.Single

class AbstractClusterRenderer<T : ClusterItem>(
        context: Context?,
        map: GoogleMap,
        clusterManager: ClusterManager<T>,
        val itemRenderer: ItemRenderer<T>) : DefaultClusterRenderer<T>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: T, markerOptions: MarkerOptions?) {
        itemRenderer.beforeRenderItem(item, markerOptions)
    }

    override fun onClusterItemRendered(clusterItem: T, marker: Marker?) {
        itemRenderer.afterRenderItem(clusterItem, marker)
    }

    interface ItemRenderer<T> {
        fun beforeRenderItem(item: T, markerOptions: MarkerOptions?)
        fun afterRenderItem(item: T, marker: Marker?)
    }
}