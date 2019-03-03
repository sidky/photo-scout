package com.github.sidky.photoscout.converter

import com.github.sidky.common.Converter
import com.github.sidky.data.paging.BoundingBox
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class LatLngBoundToBoundingBoxConverter : Converter<BoundingBox, LatLngBounds> {
    override fun convert(t: LatLngBounds): BoundingBox =
            BoundingBox(
                minLongitude = t.southwest.longitude,
                minLatitude = t.southwest.latitude,
                maxLongitude = t.northeast.longitude,
                maxLatitude = t.northeast.latitude)

}

class BoundingBoxToLatLngBoundConverter : Converter<LatLngBounds, BoundingBox> {
    override fun convert(t: BoundingBox): LatLngBounds =
            LatLngBounds(
                LatLng(t.minLatitude, t.minLongitude),
                LatLng(t.maxLatitude, t.maxLongitude))
}