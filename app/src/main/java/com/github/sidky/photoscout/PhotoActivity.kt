package com.github.sidky.photoscout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import com.github.sidky.photoscout.api.flickr.FlickrService
import com.github.sidky.photoscout.data.dao.PhotoDao
import com.google.android.flexbox.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_photo.*
import org.koin.android.ext.android.inject

class PhotoActivity : AppCompatActivity() {

    private val presenter: PhotoPresenter by inject()

    private val adapter: PhotoAdapter by lazy {
        PhotoAdapter()
    }

    private val layoutManager by lazy {
        val lm = GridLayoutManager(this, 3)
//        val lm = FlexboxLayoutManager(this)
//        lm.flexDirection = FlexDirection.ROW
//        lm.flexWrap = FlexWrap.WRAP
//        lm.alignItems = AlignItems.STRETCH
//        lm.justifyContent = JustifyContent.SPACE_BETWEEN
        lm
    }

    val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        adapter.setHasStableIds(false)
        photo_list.adapter = adapter
        photo_list.layoutManager = layoutManager
        photo_list.itemAnimator = null

        setSupportActionBar(search_bar)

        search_text.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query == null) {
                    presenter.clearSearch()
                } else {
                    presenter.search(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // ignore for now
                return false
            }

        })
    }

    override fun onStart() {
        super.onStart()

        val disposable = presenter
                .photos
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { list ->
                    adapter.submitList(list)
                }
        compositeDisposable.add(disposable)
    }

    override fun onStop() {
        super.onStop()

        compositeDisposable.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.clear()
    }
}