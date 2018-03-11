package com.github.sidky.photoscout.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.github.sidky.photoscout.PhotoAdapter
import com.github.sidky.photoscout.PhotoPresenter
import com.github.sidky.photoscout.R
import com.github.sidky.photoscout.api.flickr.BoundingBox
import com.github.sidky.photoscout.data.entity.PhotoWithSize
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.ui.IconGenerator
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_map.*
import org.koin.android.ext.android.inject
import java.util.concurrent.CountDownLatch

class PhotoClusterAdapter(val fragment: PhotoMapFragment, context: Context?, map: GoogleMap) :
        ClusterItemAdapter<PhotoMapFragment.PhotoClusterItem>(context, map, COMPARATOR){
    val iconGenerator = IconGenerator(context?.applicationContext)
    val itemImageView = ImageView(context?.applicationContext)

    init {
        iconGenerator.setContentView(itemImageView)
    }

    override fun beforeRenderItem(item: PhotoMapFragment.PhotoClusterItem, markerOptions: MarkerOptions?) {
        markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
    }

    override fun afterRenderItem(item: PhotoMapFragment.PhotoClusterItem, marker: Marker?) {
        val url = item.photo.smallestImage()?.url
        if (url != null) {
            Glide.with(fragment)
                    .asBitmap()
                    .load(url)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val currentMarker = getMarker(item)
                            if (currentMarker != null
                                    && (getCluster(currentMarker) != null || getClusterItem(currentMarker) != null)) {
                                itemImageView.setImageBitmap(resource)
                                val icon = iconGenerator.makeIcon()
                                currentMarker.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
                            }
                        }
                    })
        }
    }

    companion object {
        val COMPARATOR = object : EqualComparator<PhotoMapFragment.PhotoClusterItem> {
            override fun areContentSame(
                    oldItem: PhotoMapFragment.PhotoClusterItem,
                    newItem: PhotoMapFragment.PhotoClusterItem): Boolean =
                    oldItem.photo.equals(newItem.photo)
        }
    }
}

class PhotoMapFragment : Fragment() {

    lateinit var mapView: MapView

    val layoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    val listAdapter: PhotoAdapter by lazy {
        val newAdapter = PhotoAdapter()
        newAdapter.minDimension = 150
        newAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image_preview.layoutManager = layoutManager
        image_preview.adapter = listAdapter
    }

    val presenter: PhotoPresenter by inject()

    var compositeDisposable: CompositeDisposable? = null

    var map: GoogleMap? = null

    var adapter: ClusterItemAdapter<PhotoClusterItem>? = null

    override fun onResume() {
        super.onResume()

        mapView.onResume()

        compositeDisposable?.dispose()
        compositeDisposable = CompositeDisposable()

        mapView.getMapAsync { map ->
            this.map = map
            this.adapter = PhotoClusterAdapter(this, context, map)

            val disposable = presenter
                    .photos
                    .observeOn(AndroidSchedulers.mainThread())
                    .map {
                        listAdapter.submitList(it)
                        it.filterNotNull().filter { it.location != null }.map { PhotoClusterItem(it) }
                    }
                    .subscribe {
                        Log.e("Map", "${it.size} items")
                        adapter?.submitList(it)
                    }
            compositeDisposable?.add(disposable)

            map.setOnCameraMoveListener {
                Log.e("CAMERA", "MOVED")
                val bounds = map.projection.visibleRegion.latLngBounds
                val boundingBox = BoundingBox(bounds.southwest.longitude, bounds.southwest.latitude, bounds.northeast.longitude, bounds.northeast.latitude)
                presenter.moveMap(boundingBox)
            }
            enableMyLocation()
        }

        Log.e("COUNT", "clusters: ${adapter?.numClusters()}")
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()

        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()

        mapView.onLowMemory()
    }

    override fun onPause() {
        super.onPause()

        mapView.onPause()
        compositeDisposable?.dispose()
    }

    private fun enableMyLocation() {
        val activity = this.activity
        if (activity != null) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
            } else {
                map?.isMyLocationEnabled = true
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            map?.isMyLocationEnabled = true
        } else {
            AlertDialog.Builder(activity!!).setMessage("WTF").create().show()
        }
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

        override fun toString(): String {
            return "PhotoClusterItem(photo=$photo)"
        }
    }

    companion object {
        val LOCATION_PERMISSION_REQUEST = 23312
    }
}