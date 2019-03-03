package com.github.sidky.photoscout

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.sidky.common.Converter
import com.github.sidky.data.dao.PhotoThumbnail
import com.github.sidky.data.paging.BoundingBox
import com.github.sidky.photoscout.adapter.PhotoListAdapter
import com.github.sidky.photoscout.converter.BoundingBoxToLatLngBoundConverter
import com.github.sidky.photoscout.databinding.FragmentMapBinding
import com.github.sidky.photoscout.map.ThumbnailItem
import com.github.sidky.photoscout.map.ThumbnailRenderer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class PhotoMapFragment : Fragment() {

    private lateinit var binding: FragmentMapBinding
    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<ThumbnailItem>? = null

    private val photoViewModel: PhotoListViewModel by sharedViewModel()
    private val boundingBoxConverter: Converter<BoundingBox, LatLngBounds> by inject()
    private val latLngBoundsConverter: BoundingBoxToLatLngBoundConverter by inject()

    private lateinit var adapter: PhotoListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        binding.map.onCreate(savedInstanceState)

        adapter = PhotoListAdapter(R.id.action_display)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.thumbnailList.layoutManager = layoutManager
        binding.thumbnailList.adapter = adapter

        binding.map.getMapAsync {
            googleMap = it

            clusterManager = ClusterManager(activity, googleMap)
            clusterManager?.renderer = ThumbnailRenderer(activity!!, it, clusterManager!!)

            clusterManager?.setOnClusterItemClickListener {
                Timber.d("ID to send: ${it.id()}")
                val arg = PhotoMapFragmentDirections.actionDisplay(photoId = it.id())
                findNavController().navigate(arg)
                true
            }
            googleMap?.setOnCameraIdleListener(clusterManager)
            googleMap?.setOnMarkerClickListener(clusterManager)
            googleMap?.setOnInfoWindowClickListener(clusterManager)

            if (!enableMyLocation()) {
                requestPermission()
            }

            googleMap?.setOnCameraMoveListener {
                val updatedArea = currentBounds()

                binding.enableSearchHere = updatedArea != photoViewModel.searchArea
                binding.executePendingBindings()
            }

            photoViewModel.thumbnailsForMap().observe(this, Observer {
                Timber.d("CLEARING")
                clusterManager?.clearItems()

                it.forEach {
                    Timber.tag("MAP").d("${it.id} ${it.url}")
                }

                clusterManager?.addItems(it.map { ThumbnailItem(it) })
                clusterManager?.cluster()

                moveToSelectedArea()
            })
        }

        photoViewModel.photoLiveData.observe(this, Observer<PagedList<PhotoThumbnail>>() {
            adapter.submitList(it)
        })

        binding.searchHere.background = ContextCompat.getDrawable(activity!!, R.drawable.search_here_button_background)
        binding.searchHere.backgroundTintList = null

        binding.searchAction = View.OnClickListener {
            val updatedArea = currentBounds()
            photoViewModel.searchArea = updatedArea
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()

        moveToSelectedArea()
    }

    private fun moveToSelectedArea() {
        val selectedArea = photoViewModel.searchArea
        Timber.d("Camera: ${selectedArea}")
        if (selectedArea != null) {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsConverter.convert(selectedArea), 20))
        }

    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
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

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST ->
                if (grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                }
        }
    }

    private fun enableMyLocation(): Boolean {
        val ctx = context
        return if (ctx != null && ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
            true
        } else {
            false
        }
    }

    private fun currentBounds(): BoundingBox? {
        val bounds = googleMap?.projection?.visibleRegion?.latLngBounds
        return if (bounds != null) boundingBoxConverter.convert(bounds) else null
    }

    companion object {
        private const val LOCATION_REQUEST: Int = 38936
    }
}