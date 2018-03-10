package com.github.sidky.photoscout.map

import android.support.v4.app.Fragment
import android.util.Log
import com.github.sidky.photoscout.PhotoPresenter
import com.github.sidky.photoscout.data.entity.PhotoWithSize
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject

class PhotoMapFragment : SupportMapFragment() {

    val presenter: PhotoPresenter by inject()

    var compositeDisposable: CompositeDisposable? = null

    var map: GoogleMap? = null

    var adapter: ClusterItemAdapter<PhotoClusterItem>? = null

    override fun onResume() {
        super.onResume()

        compositeDisposable?.dispose()
        compositeDisposable = CompositeDisposable()

        getMapAsync { map ->
            this.map = map
            this.adapter = ClusterItemAdapter(context, map,
                    object : EqualComparator<PhotoClusterItem> {
                        override fun areContentSame(oldItem: PhotoClusterItem, newItem: PhotoClusterItem): Boolean =
                                oldItem.photo.equals(newItem.photo)

                    })

            val disposable = presenter
                    .photos
                    .map {
                        it.filterNotNull().filter { it.location != null }.map { PhotoClusterItem(it) }
                    }
                    .subscribe {
                        Log.e("Map", "${it.size} items")
                        adapter?.submitList(it)
                    }
            compositeDisposable?.add(disposable)
        }
    }

    override fun onPause() {
        super.onPause()

        compositeDisposable?.dispose()
    }

    class PhotoClusterItem(val photo: PhotoWithSize) : ClusterItem {
        override fun getSnippet(): String = ""

        override fun getTitle(): String = photo.title

        override fun getPosition(): LatLng {
            val location = photo.location
            return if (location != null) {
                LatLng(location.latitude, location.longitude)
            } else {
                LatLng(0.0, 0.0)
            }
        }

    }
}