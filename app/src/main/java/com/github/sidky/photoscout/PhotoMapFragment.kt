package com.github.sidky.photoscout

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.github.sidky.photoscout.data.PhotoWithURL
import com.github.sidky.photoscout.databinding.MapFragmentBinding
import com.github.sidky.photoscout.maps.PhotoClusterItem
import com.github.sidky.photoscout.maps.PhotoClusterRenderer
import com.github.sidky.photoscout.util.DiffCallback
import com.github.sidky.photoscout.viewmodel.ActionBarViewModel
import com.github.sidky.photoscout.viewmodel.PhotoMapViewModel
import com.github.sidky.photoscout.viewmodel.VisibleScreen
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PhotoMapFragment : Fragment(), DiffCallback<PhotoWithURL> {

    lateinit var binding: MapFragmentBinding
    private var clusterManager: ClusterManager<PhotoClusterItem>? = null
    private var googleMap: GoogleMap? = null

    private val markerMap: MutableMap<String, Marker> = mutableMapOf()
    private val markerMapMutex = Mutex()

    private val mapViewModel:PhotoMapViewModel by viewModel()

    private val backgroundScope: CoroutineDispatcher by inject("background")
    private val foregroundScope: CoroutineDispatcher by inject("foreground")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.map_fragment, container, false)
        binding.map.onCreate(savedInstanceState)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.map.getMapAsync { googleMap ->
            this.googleMap = googleMap
            if (!hasPermission()) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 0)
            } else {
                googleMap.isMyLocationEnabled = true
            }

            val cm: ClusterManager<PhotoClusterItem> = ClusterManager(context, googleMap)
            clusterManager = cm
            googleMap.setOnCameraIdleListener(clusterManager)
            googleMap.setOnMarkerClickListener(clusterManager)
            googleMap.setOnInfoWindowClickListener(clusterManager)
            clusterManager?.renderer = PhotoClusterRenderer(context!!, googleMap, cm, backgroundScope, foregroundScope)

            mapViewModel.attach().observe(this, Observer {

            })

            mapViewModel.subscribeDiff(this, this)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (hasPermission()) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()

        getSharedViewModel<ActionBarViewModel>().setActionBarScreen(VisibleScreen.MAP)
    }

    override fun onStop() {
        super.onStop()
        binding.map.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.map.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    override fun add(item: PhotoWithURL) {
        runBlocking {
            item.photo.location?.let {
                val item = PhotoClusterItem(item)
                clusterManager?.addItem(item)
//                val marker = googleMap?.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
//                marker?.tag = item
//                if (marker != null) {
//                    markerMapMutex.withLock {
//                        markerMap.put(item.photo.photoId, marker)
//                    }
//                }
                clusterManager?.cluster()
            }
        }
    }

    override fun update(oldItem: PhotoWithURL, newItem: PhotoWithURL) {
        clusterManager?.addItem(PhotoClusterItem(newItem))
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(item: PhotoWithURL) {
        runBlocking {
            val marker = markerMapMutex.withLock {
                markerMap.remove(item.photo.photoId)
            }
            marker?.remove()
        }
    }

    private fun hasPermission() = ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}