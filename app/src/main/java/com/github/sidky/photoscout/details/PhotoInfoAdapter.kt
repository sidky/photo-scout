package com.github.sidky.photoscout.details

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.sidky.photoscout.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.*

abstract class AbstractInfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: PhotoDetailItem)
}

enum class InfoItemType {
    TITLE, OWNER, DESCRIPTION, LOCATION
}

class TextInfoViewHolder(val view: TextView): AbstractInfoViewHolder(view) {
    override fun bind(item: PhotoDetailItem) {
        when (item) {
            is PhotoDetailItem.Title -> view.text = item.title
            is PhotoDetailItem.Owner ->
                view.text = view.context.getString(R.string.by_author, item.ownerName)
            is PhotoDetailItem.Description -> view.text = item.description
            else -> Log.e("Info", "Invalid item: ${item} for text info view holder")
        }
    }
}

class LocationInfoViewHolder(val mapView: MapView): AbstractInfoViewHolder(mapView), OnMapReadyCallback {

    var map: GoogleMap? = null

    override fun onMapReady(map: GoogleMap?) {
        MapsInitializer.initialize(mapView.context.applicationContext)
        this.map = map
        if (map != null) {
            setLocation()
        }
    }

    init {
        mapView.onCreate(null)
        mapView.getMapAsync(this)
    }

    override fun bind(item: PhotoDetailItem) {
        if (item is PhotoDetailItem.Location) {
            val location = LatLng(item.latitude, item.longitude)
            mapView.setTag(TAG_ID, location)
            setLocation()
        }
    }

    private fun setLocation() {
        val map = this.map

        if (map != null) {
            val location = mapView.getTag(TAG_ID) as LatLng?

            if (location != null) {
                Log.i("MAP", "Moving to: (${location.latitude}, ${location.longitude})")
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13.0f))
                map.addMarker(MarkerOptions().position(location))

                map.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        }

    }


    companion object {
        val TAG_ID = UUID.randomUUID().leastSignificantBits.toInt()
    }
}

class PhotoInfoAdapter : RecyclerView.Adapter<AbstractInfoViewHolder>() {
    override fun getItemViewType(position: Int): Int =
        when (items.get(position)) {
            is PhotoDetailItem.Title -> InfoItemType.TITLE
            is PhotoDetailItem.Description -> InfoItemType.DESCRIPTION
            is PhotoDetailItem.Owner -> InfoItemType.OWNER
            is PhotoDetailItem.Location -> InfoItemType.LOCATION
            else -> null
        }?.ordinal ?: -1

    var items: List<PhotoDetailItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractInfoViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        return when (InfoItemType.values()[viewType]) {
            InfoItemType.TITLE -> {
                val v = inflator.inflate(R.layout.info_title, parent, false) as TextView
                TextInfoViewHolder(v)
            }
            InfoItemType.OWNER -> {
                val v = inflator.inflate(R.layout.info_owner, parent, false) as TextView
                TextInfoViewHolder(v)
            }
            InfoItemType.DESCRIPTION -> {
                val v = inflator.inflate(R.layout.info_description, parent, false) as TextView
                TextInfoViewHolder(v)
            }
            InfoItemType.LOCATION -> {
                val v = inflator.inflate(R.layout.info_location, parent, false) as MapView
                LocationInfoViewHolder(v)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AbstractInfoViewHolder, position: Int) {
        holder.bind(items.get(position))
    }


}