package com.github.sidky.data.dao

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

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

    fun resetToInteresring() {
        searchType = SearchType.INTERESTING
        query = null
        hasNext = true
        next = 0
    }

    fun resetToSearch(query: String) {
        searchType = SearchType.SEARCH
        this.query = query
        hasNext = true
        next = 0
    }
}

enum class SearchType {
    NONE, INTERESTING, SEARCH
}