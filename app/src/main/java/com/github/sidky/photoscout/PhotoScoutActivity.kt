package com.github.sidky.photoscout

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.github.sidky.photoscout.databinding.ActivityPhotoBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class PhotoScoutActivity : AppCompatActivity() {

    lateinit var binding: ActivityPhotoBinding

    lateinit var navHost: NavHostFragment

    var menu: Menu? = null

    val photoViewModel: PhotoListViewModel by viewModel()

    private val destinationListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when (destination.id) {
            R.id.photoDisplayFragment, R.id.loginFragment -> supportActionBar?.hide()
            R.id.photoListFragment -> {
                supportActionBar?.show()
                menu?.findItem(R.id.grid)?.isVisible = false
                menu?.findItem(R.id.map)?.isVisible = true
            }
            R.id.photoMapFragment -> {
                supportActionBar?.show()
                menu?.findItem(R.id.grid)?.isVisible = true
                menu?.findItem(R.id.map)?.isVisible = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayShowHomeEnabled(false)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_photo)

        navHost = NavHostFragment.create(R.navigation.nav_graph)

        supportFragmentManager.beginTransaction().replace(R.id.container, navHost).setPrimaryNavigationFragment(navHost).commit()

        photoViewModel.attach(this)
    }

    override fun onStart() {
        super.onStart()

        navHost.navController.addOnDestinationChangedListener(destinationListener)
    }

    override fun onStop() {
        super.onStop()

        navHost.navController.removeOnDestinationChangedListener(destinationListener)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_action, menu)
        this.menu = menu

        val searchView = menu?.findItem(R.id.search)?.actionView

        if (searchView != null && searchView is SearchView) {
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        photoViewModel.search(query)
                    } else {
                        photoViewModel.loadInteresting()
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Timber.d("Change: $newText")
                    return false
                }
            })

            searchView.setOnCloseListener {
                photoViewModel.loadInteresting()
                true
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.map -> navHost.navController.navigate(PhotoListFragmentDirections.actionMap())
            R.id.grid -> navHost.navController.navigate(PhotoMapFragmentDirections.actionList())
        }
        return true
    }
}