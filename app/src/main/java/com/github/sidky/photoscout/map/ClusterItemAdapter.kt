package com.github.sidky.photoscout.map

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.v4.util.ArraySet
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import kotlin.math.max
import kotlin.math.min

abstract class ClusterItemAdapter<T : ClusterItem>(context: Context?,
                                          val map: GoogleMap,
                                          cmp: EqualComparator<T>) : AbstractClusterRenderer.ItemRenderer<T> {

    private val clusterManager: ClusterManager<T> = ClusterManager(context, map)
    private val itemManager: ClusterItemManager<T> = ClusterItemManager.DefaultClusterItemManager(cmp)
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private val renderer = AbstractClusterRenderer<T>(context, map, clusterManager, this)

    init {
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        clusterManager.renderer = renderer
    }


    fun submitList(items: List<T>) {
        mainThreadHandler.post {
            clusterManager.clearItems()
            map.clear()
//            map.clear()

//            diff.deletes.forEach {
//                removeFromCluster(clusterManager, it)
//            }
            val builder = LatLngBounds.builder()
            items.forEach {
                addToCluster(clusterManager, it)
                builder.include(it.position)
            }

            if (items.isEmpty()) {
                builder.include(LatLng(0.0, 0.0))
            }
            Log.e("Cluster", "Cluster size: ${clusterManager.markerCollection.markers.size} + ${clusterManager.clusterMarkerCollection.markers.size}")

//            map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10))
        }
    }

    fun addToCluster(clusterManager: ClusterManager<T>, item: T) {
        clusterManager.addItem(item)
    }

    fun removeFromCluster(clusterManager: ClusterManager<T>, item: T) {
        clusterManager.removeItem(item)
    }

    fun getMarker(item: T) = renderer.getMarker(item)

    fun getCluster(marker: Marker): Cluster<T>? = renderer.getCluster(marker)
    fun getClusterItem(marker: Marker):T? = renderer.getClusterItem(marker)

    fun numClusters() = clusterManager.markerCollection.markers.size
}

data class ClusterItemDiff<out T: ClusterItem>(
        val inserts: List<T>,
        val deletes: List<T>)

interface EqualComparator<T> {
    fun areContentSame(oldItem: T, newItem: T): Boolean
}

sealed class ClusterItemManager<T : ClusterItem>(val comparator: EqualComparator<T>) {
    abstract fun submitList(items: List<T>): ClusterItemDiff<T>

    abstract fun zoomArea(): LatLngBounds

    class DefaultClusterItemManager<T: ClusterItem>(cmp: EqualComparator<T>) : ClusterItemManager<T>(cmp) {

        private val existingItems: MutableList<T> = mutableListOf()

        override fun zoomArea(): LatLngBounds {
            if (existingItems.size > 0) {
                val bound = existingItems
                        .map { it.position }
                        .fold(LatLngBounds.Builder(), { builder, position ->
                            builder.include(position)
                        }).build()
                return bound.pad(10.0)
            } else {
                return LatLngBounds.Builder().include(LatLng(0.0, 0.0)).build()
            }
        }

        override fun submitList(items: List<T>): ClusterItemDiff<T> {
            val deletes = ArraySet<T>()
            val inserts = ArraySet<T>()

            for (oldItem in this.existingItems) {
                if (items.find { comparator.areContentSame(oldItem, it) } == null) {
                    deletes.add(oldItem)
                }
            }

            for (newItem in items) {
                if (this.existingItems.find { comparator.areContentSame(it, newItem) } == null) {
                    inserts.add(newItem)
                }
            }

            this.existingItems.clear()
            this.existingItems.addAll(items)

            return ClusterItemDiff(inserts = inserts.toList(), deletes = deletes.toList())
        }
    }
}

fun LatLngBounds.pad(amount: Double): LatLngBounds =
    LatLngBounds(
            LatLng(this.southwest.latitude - amount, this.southwest.longitude - amount),
            LatLng(this.northeast.latitude + amount, this.northeast.longitude + amount)
            )
