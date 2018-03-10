package com.github.sidky.photoscout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import com.github.sidky.photoscout.list.PhotoListFragment
import com.github.sidky.photoscout.map.PhotoMapFragment
import kotlinx.android.synthetic.main.activity_photo.*
import org.koin.android.ext.android.inject
import org.koin.standalone.KoinComponent
import org.koin.standalone.releaseContext

class PhotoActivity : AppCompatActivity(), KoinComponent {

    private val presenter: PhotoPresenter by inject()

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

//        showList()
        showMap()
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