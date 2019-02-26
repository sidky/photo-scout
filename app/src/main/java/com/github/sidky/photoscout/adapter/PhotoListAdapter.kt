package com.github.sidky.photoscout.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.Navigation
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fivehundredpx.greedolayout.GreedoLayoutSizeCalculator
import com.github.sidky.data.dao.PhotoThumbnail
import com.github.sidky.photoscout.PhotoListFragmentDirections
import com.github.sidky.photoscout.R
import com.github.sidky.photoscout.databinding.ItemPhotoBinding
import timber.log.Timber

class PhotoViewHolder(private val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

    fun update(photo: PhotoThumbnail) {
        Timber.i("Photo: ${photo.url}: ${photo.width}x${photo.height}")
        Glide.with(binding.root).load(photo.url).override(photo.width, photo.height).into(binding.thumbnail)

        val arg = PhotoListFragmentDirections.actionPhotoListFragmentToPhotoDisplayFragment(photo.photoId)

        binding.thumbnail.setOnClickListener(Navigation.createNavigateOnClickListener(
            R.id.action_photoListFragment_to_photoDisplayFragment,
            arg.arguments))

        binding.executePendingBindings()
    }
}

class PhotoListAdapter : PagedListAdapter<PhotoThumbnail, PhotoViewHolder>(diffCallback), GreedoLayoutSizeCalculator.SizeCalculatorDelegate {

    private val mediatorClickLiveData = MediatorLiveData<PhotoThumbnail>()

    override fun aspectRatioForIndex(position: Int): Double {
        if (position >= itemCount) {
            return 1.0
        }
        val item = getItem(position)
        val width = item?.width
        val height = item?.height

        if (width != null && height != null) {
            return width.toDouble() / height.toDouble()
        } else {
            return 0.0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding: ItemPhotoBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_photo, parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val thumbnail = getItem(position)

        Timber.d("Bind: $position:  ${thumbnail?.id} ${thumbnail?.url}")

        if (thumbnail != null) {
            holder.update(thumbnail)
        }
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<PhotoThumbnail>() {
            override fun areItemsTheSame(oldItem: PhotoThumbnail, newItem: PhotoThumbnail): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PhotoThumbnail, newItem: PhotoThumbnail): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}