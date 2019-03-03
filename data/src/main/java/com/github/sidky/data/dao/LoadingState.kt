package com.github.sidky.data.dao

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.github.sidky.data.paging.BoundingBox

class LoadingState(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences("loading_state", Context.MODE_PRIVATE)
    }

    var searchType: SearchType
        get() = SearchType.values()[sharedPreferences.getInt("search_type", SearchType.NONE.ordinal)]
        set(value) = sharedPreferences.edit(commit = true) {  putInt("search_type", value.ordinal) }

    var query: String?
        get() = sharedPreferences.getString("query", null)
        set(value) = sharedPreferences.edit(commit = true) {  putString("query", value) }

    var hasNext: Boolean
        get() = sharedPreferences.getBoolean("has_next", false)
        set(value) = sharedPreferences.edit(commit = true) {  putBoolean("has_next", value) }

    var next: Int
        get() = sharedPreferences.getInt("next", -1)
        set(value) = sharedPreferences.edit(commit = true) { putInt("next", value) }

    private var minLatitude: Float?
        get() = getFloat("min_latitude")
        set(value) = setFloat("min_latitude", value)

    private var minLongitude: Float?
        get() = getFloat("min_longitude")
        set(value) = setFloat("min_longitude", value)

    private var maxLatitude: Float?
        get() = getFloat("max_latitude")
        set(value) = setFloat("max_latitude", value)

    private var maxLongitude: Float?
        get() = getFloat("max_longitude")
        set(value) = setFloat("max_longitude", value)

    var boundingBox: BoundingBox?
        get() {
            val minLatitude = minLatitude
            val minLongitude = minLongitude
            val maxLatitude = maxLatitude
            val maxLongitude = maxLongitude

            if (minLatitude != null && minLongitude != null && maxLatitude != null && maxLongitude != null) {
                return BoundingBox(minLongitude = minLongitude.toDouble(),
                    minLatitude = minLatitude.toDouble(),
                    maxLongitude = maxLongitude.toDouble(),
                    maxLatitude = maxLatitude.toDouble())
            } else {
                return null
            }
        }
        set(value) {
            minLatitude = value?.minLatitude?.toFloat()
            minLongitude = value?.minLongitude?.toFloat()
            maxLatitude = value?.maxLatitude?.toFloat()
            maxLongitude = value?.maxLongitude?.toFloat()
        }

    fun resetToInteresting(box: BoundingBox? = null) {
        searchType = SearchType.INTERESTING
        query = null
        boundingBox = box
        hasNext = true
        next = 0
    }

    fun resetToSearch(query: String) {
        searchType = SearchType.SEARCH
        this.query = query
        hasNext = true
        next = 0
    }

    private fun getFloat(key: String): Float? {
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.getFloat(key, 0.0f)
        } else {
            null
        }
    }

    private fun setFloat(key: String, value: Float?) {
        sharedPreferences.edit(commit = false) {
            if (value == null) {
                this.remove(key)
            } else {
                this.putFloat(key, value)
            }
        }
    }
}

enum class SearchType {
    NONE, INTERESTING, SEARCH
}