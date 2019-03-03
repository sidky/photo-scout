package com.github.sidky.photoscout

import android.os.Bundle
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.github.sidky.data.dao.PhotoThumbnail
import com.github.sidky.data.paging.BoundingBox
import com.github.sidky.data.repository.PhotoDetail
import com.github.sidky.data.repository.PhotoRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class PhotoListViewModel(private val repository: PhotoRepository) : ViewModel() {

    private val actionLiveData = MediatorLiveData<Unit>()

    val photoLiveData = MutableLiveData<PagedList<PhotoThumbnail>>()

    var searchArea: BoundingBox? = null
        set(value) {
            field = value
            loadPhotos()
        }
    private var currentQuery: Query = Query.Interesting()

    fun thumbnailsForMap() = repository.thumbnailsForMap()

    private var listLiveData: LiveData<PagedList<PhotoThumbnail>>? = null
        set(value) {
            val prev = field
            if (prev != null) {
                actionLiveData.removeSource(prev)
            }
            if (value != null) {
                actionLiveData.addSource(value) {
                    Timber.d("Loaded")
                    photoLiveData.value = it
                }
            }
            field = value
        }

    fun loadInteresting() {
        val listing = repository.loadInteresting()
        currentQuery = Query.Interesting()

        listLiveData = listing.pagedList
    }

    fun search(query: String) {
        val listing = repository.loadSearch(query)
        currentQuery = Query.Search(query)

        listLiveData = listing.pagedList
    }

    suspend fun detail(photoId: String): PhotoDetail? {
        return repository.detail(photoId)
    }

    private fun loadPhotos() {
        val area = searchArea
        val query = this.currentQuery

        when (query) {
            is Query.Interesting -> {
                if (area != null) {
                    repository.loadInteresting(area)
                } else {
                    repository.loadInteresting()
                }
            }
            is Query.Search -> {
                repository.loadSearch(query.query)
            }

        }
    }

    fun attach(lifecycleOwner: LifecycleOwner) {
        GlobalScope.launch {
            loadInteresting()
        }
        actionLiveData.observe(lifecycleOwner, Observer<Unit> { })
    }

    fun saveInstance(outState: Bundle) {
        val q = currentQuery
        when (q) {
            is Query.Interesting -> outState.putInt("type", QueryType.INTERESTING.ordinal)
            is Query.Search -> {
                outState.putInt("type", QueryType.SEARCH.ordinal)
                outState.putString("query", q.query)
            }
        }
        outState.putParcelable("box", searchArea)
    }

    fun restoreInstance(inState: Bundle) {
        val t = QueryType.values()[inState.getInt("type")]
        val query = if (t == QueryType.INTERESTING) {
            Query.Interesting()
        } else {
            val q = inState.getString("query")
            Query.Search(q)
        }
        val box: BoundingBox? = inState.getParcelable("box")

        this.currentQuery = query
        this.searchArea = box
    }

    private sealed class Query {
        class Interesting(): Query()
        data class Search(val query: String): Query()
    }

    private enum class QueryType {
        INTERESTING, SEARCH
    }
}