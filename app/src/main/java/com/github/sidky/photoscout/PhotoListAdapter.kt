package com.github.sidky.photoscout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.sidky.photoscout.data.Photo
import com.github.sidky.photoscout.data.PhotoWithURL
import com.github.sidky.photoscout.databinding.PhotoItemBinding
import timber.log.Timber

typealias ClickListener = (View, PhotoWithURL?) -> Unit

class PhotoListAdapter(private val clickListener: ClickListener) :
    PagedListAdapter<PhotoWithURL, PhotoListAdapter.Companion.PhotoListViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoListViewHolder {
        val binding = DataBindingUtil.inflate<PhotoItemBinding>(LayoutInflater.from(parent.context), R.layout.photo_item, parent,false)
        val vh = PhotoListViewHolder(binding)
        binding.photo.setOnClickListener {
            clickListener(it, getItem(vh.adapterPosition))
        }
        return vh
    }

    override fun onBindViewHolder(holder: PhotoListViewHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.setPhoto(item)
        }
    }

    companion object {

        val diffCallback = object : ItemCallback<PhotoWithURL>() {
            override fun areItemsTheSame(oldItem: PhotoWithURL, newItem: PhotoWithURL): Boolean {
                return oldItem.photo.id == newItem.photo.id
            }

            override fun areContentsTheSame(oldItem: PhotoWithURL, newItem: PhotoWithURL): Boolean {
                return oldItem.urls == newItem.urls
            }
        }

        class PhotoListViewHolder(val binding: PhotoItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun setPhoto(photo: PhotoWithURL) {
                Timber.e("Size ${binding.photo.width} ${binding.photo.height}")

                Glide.with(binding.root).load(photo.urls[4].url).into(binding.photo)

                binding.photo.transitionName = photo.photo.photoId
            }
        }
    }
}

