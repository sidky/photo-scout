package com.github.sidky.photoscout

import android.graphics.Bitmap
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.github.sidky.photoscout.databinding.PhotoDetailBinding
import com.github.sidky.photoscout.viewmodel.ActionBarViewModel
import com.github.sidky.photoscout.viewmodel.PhotoDetailViewModel
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PhotoDetailFragment : Fragment() {
    lateinit var binding: PhotoDetailBinding
    lateinit var safeArgs: PhotoDetailFragmentArgs

    val detailsViewModel: PhotoDetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onResume() {
        super.onResume()

        getSharedViewModel<ActionBarViewModel>().showActionBar(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.photo_detail, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        safeArgs = PhotoDetailFragmentArgs.fromBundle(arguments)

        binding.photo.transitionName = safeArgs.transitionName
        detailsViewModel.loadDetails(safeArgs.photoId)

        Glide.with(this)
            .asBitmap()
            .load(safeArgs.url)
            .into(object : BitmapImageViewTarget(binding.photo) {
                override fun setResource(resource: Bitmap?) {
                    binding.photo.setImageBitmap(resource)
                    startPostponedEnterTransition()
                }

            })
    }
}