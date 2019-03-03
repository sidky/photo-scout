package com.github.sidky.photoscout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.fivehundredpx.greedolayout.GreedoLayoutManager
import com.github.sidky.data.dao.PhotoThumbnail
import com.github.sidky.photoscout.adapter.PhotoListAdapter
import com.github.sidky.photoscout.databinding.FragmentPhotoListBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PhotoListFragment : Fragment() {

    private lateinit var binding: FragmentPhotoListBinding

    private val photoViewModel: PhotoListViewModel by sharedViewModel()

    private lateinit var adapter: PhotoListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_list, container, false)

        val adapter = PhotoListAdapter(R.id.action_photoListFragment_to_photoDisplayFragment)
        this.adapter = adapter

        val rv = binding.photoList
        rv.adapter = adapter


        val lm = GreedoLayoutManager(adapter)
//        val lm = GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
//        lm.setFixedHeight(true)
//        val lm = LinearLayoutManager(context)
        rv.layoutManager = lm
        rv.itemAnimator = null

        photoViewModel.photoLiveData.observe(this,
            Observer<PagedList<PhotoThumbnail>> { t ->
                adapter.submitList(t)
            })

        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            binding.photoList.layoutManager?.onRestoreInstanceState(savedInstanceState.getParcelable("scroll"))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val s = binding.photoList.layoutManager?.onSaveInstanceState()
        outState.putParcelable("scroll", s)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}