package com.github.sidky.photoscout

import android.arch.paging.PagedListAdapter
import android.content.Intent
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.sidky.photoscout.data.entity.PhotoWithSize
import com.github.sidky.photoscout.details.PhotoDetails
import com.github.sidky.photoscout.details.PhotoDetailsActivity

data class PhotoViewHolder(val view: AppCompatImageView) : RecyclerView.ViewHolder(view)

class PhotoDiffCallback : DiffUtil.ItemCallback<PhotoWithSize>() {
    override fun areItemsTheSame(oldItem: PhotoWithSize, newItem: PhotoWithSize): Boolean =
            oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: PhotoWithSize, newItem: PhotoWithSize): Boolean =
        oldItem.id == newItem.id
}

class PhotoAdapter : PagedListAdapter<PhotoWithSize, PhotoViewHolder>(PhotoDiffCallback()) {

    var minDimension: Int = MIN_IMAGE_DIMENSION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder =
        PhotoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent,false) as AppCompatImageView)

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo : PhotoWithSize? = getItem(position)
        val largest = photo?.largestImage()

        if (photo != null && largest != null) {
            val requestOptions = RequestOptions().override(Target.SIZE_ORIGINAL).fitCenter()
            Glide.with(holder.view)
                    .load(photo.smallestImage(minDimension)?.url).apply(requestOptions)
                    .into(holder.view)
            holder.view.setOnClickListener(clickHandler(holder.view.context as AppCompatActivity,
                    PhotoDetails(photo.id, photo.title, largest.url)))
        }
    }

    companion object {
        val MIN_IMAGE_DIMENSION = 300
    }

    private class clickHandler(private val activity: AppCompatActivity,
                               private val photo: PhotoDetails): View.OnClickListener {
        override fun onClick(v: View?) {
            Log.e("Adapter", "Sharing: ${v}")
            val intent = Intent(activity, PhotoDetailsActivity::class.java)
            intent.putExtra(PhotoDetailsActivity.PHOTO, photo)

            val option: ActivityOptionsCompat = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(activity,
                            v!!,
                            ViewCompat.getTransitionName(v))
            activity.startActivity(intent, option.toBundle())
        }

    }
}