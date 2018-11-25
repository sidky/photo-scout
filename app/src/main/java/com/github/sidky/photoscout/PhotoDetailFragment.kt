package com.github.sidky.photoscout

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.*
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.github.sidky.photoscout.databinding.PhotoDetailBinding
import timber.log.Timber

class PhotoDetailFragment : Fragment() {
    lateinit var binding: PhotoDetailBinding
    lateinit var safeArgs: PhotoDetailFragmentArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
//        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.photo_detail, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        safeArgs = PhotoDetailFragmentArgs.fromBundle(arguments)

        binding.photo.transitionName = safeArgs.transitionName

        val target = Glide.with(this)
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