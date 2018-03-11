package com.github.sidky.photoscout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.View
import com.github.sidky.photoscout.list.PhotoListFragment
import com.github.sidky.photoscout.map.PhotoMapFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_photo.*
import org.koin.android.ext.android.inject
import org.koin.standalone.KoinComponent
import org.koin.standalone.releaseContext

class PhotoActivity : AppCompatActivity(), KoinComponent {

    private val presenter: PhotoPresenter by inject()

    private val compositeDisposable: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

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

        refresh.setOnClickListener {
            presenter.refresh()
        }

        switch_button.setOnClickListener {
            presenter.switch()
        }
    }

    override fun onPause() {
        super.onPause()

        compositeDisposable?.dispose()
    }

    override fun onResume() {
        super.onResume()

        compositeDisposable?.dispose()
        val screenTransitionDisposable = presenter
                .screenState()
                .map { it.screen }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it == PhotoPresenter.Screen.LIST) {
                        showList()
                    } else if (it == PhotoPresenter.Screen.MAP) {
                        showMap()
                    }
                }

        val loadingDisposable = presenter
                .screenState()
                .map { it.isLoading }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    refresh.visibility = if (it) View.GONE else View.VISIBLE
                    refresh_progress.visibility = if (it) View.VISIBLE else View.GONE
                }
        compositeDisposable?.addAll(loadingDisposable, screenTransitionDisposable)
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.clear()
        releaseContext("photo.list")
    }

    fun showList() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PhotoListFragment())
                .commit()
    }

    fun showMap() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PhotoMapFragment())
                .commit()
    }
}