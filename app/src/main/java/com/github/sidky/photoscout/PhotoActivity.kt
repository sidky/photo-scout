package com.github.sidky.photoscout

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.github.sidky.photoscout.databinding.PhotoActivityBinding
import com.github.sidky.photoscout.viewmodel.ActionBarState
import com.github.sidky.photoscout.viewmodel.ActionBarViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class PhotoActivity : AppCompatActivity() {

    val actionBarState: ActionBarViewModel by viewModel()

    val viewModel: PhotoViewModel by viewModel()

    lateinit var binding: PhotoActivityBinding

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

        viewModel.attach(this)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        actionBarState.state.observe(this, Observer<ActionBarState> {
            if (it.isVisible) {
                supportActionBar?.show()
            } else {
                supportActionBar?.hide()
            }
        })

        call()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar, menu)
        val searchView = menu?.findItem(R.id.action_search)
        searchView?.actionView?.let {
            if (it is SearchView) {
                it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        viewModel.search(query)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        // Do nothing
                        return false
                    }
                })
            }
        }
        return true
    }

    fun call() = runBlocking{
        Timber.e("Inside call")
        async {
            Timber.e("Inside async")
            viewModel.loadInteresting()
        }
    }

    override fun onSupportNavigateUp(): Boolean  = findNavController(findViewById(R.id.fragment)).navigateUp()
}