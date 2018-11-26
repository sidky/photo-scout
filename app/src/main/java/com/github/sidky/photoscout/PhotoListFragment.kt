package com.github.sidky.photoscout

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sidky.photoscout.data.PhotoWithURL
import com.github.sidky.photoscout.databinding.ListFragmentBinding
import com.github.sidky.photoscout.viewmodel.ActionBarViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PhotoListFragment : Fragment() {

    lateinit var binding: ListFragmentBinding

    val viewModel: PhotoViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.list_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PhotoListAdapter(this::onPhotoClick)
        val rv = binding.photoList
        rv.adapter = adapter
//        var m = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        var m = GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
//        m.flexWrap = FlexWrap.WRAP
//        m.flexDirection = FlexDirection.ROW
//        m.justifyContent = JustifyContent.SPACE_BETWEEN
//        m.alignItems = AlignItems.STRETCH
        rv.layoutManager = m
//        rv.itemAnimator = null

        viewModel.setMaxDimension(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100.0f, resources.displayMetrics).toInt())

        viewModel.photoLiveData.observe(this, Observer<PagedList<PhotoWithURL>> {
            adapter.submitList(it)
        })
    }

    override fun onResume() {
        super.onResume()

        getSharedViewModel<ActionBarViewModel>().showActionBar(true)
    }

    private fun onPhotoClick(source: View, photo: PhotoWithURL?) {
        val largest = photo?.urls?.maxBy { it.width * it.height }
        largest?.let {
            val transition = ViewCompat.getTransitionName(source) ?: ""
            val extras = FragmentNavigatorExtras(source to transition)
            val action = PhotoListFragmentDirections.OpenDetail(photo.photo.photoId, transition, largest.url)
            findNavController().navigate(action, extras)
        }
    }
}