package com.github.sidky.photoscout

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.PagedList
import com.github.sidky.photoscout.data.PhotoWithURL
import com.github.sidky.photoscout.data.model.Photo
import com.github.sidky.photoscout.databinding.PhotoActivityBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PhotoActivity : AppCompatActivity() {

    val presenter: PhotoPresenter by inject()

    lateinit var binding: PhotoActivityBinding

    val coroutineContext = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.photo_activity)

        val fragment = PhotoListFragment()

        val finalHost = NavHostFragment.create(R.navigation.nav_graph)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment, finalHost)
            .setPrimaryNavigationFragment(finalHost)
            .commit()

        presenter.attach(this)

//        Timber.e("Before call")
        call()
    }

    fun call() = runBlocking{
        Timber.e("Inside call")
        async {
            Timber.e("Inside async")
            presenter.loadInteresting()
        }
    }

    override fun onSupportNavigateUp(): Boolean  = findNavController(findViewById(R.id.fragment)).navigateUp()
}