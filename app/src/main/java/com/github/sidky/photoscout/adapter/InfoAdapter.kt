package com.github.sidky.photoscout.adapter

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.sidky.data.repository.PhotoDetail
import com.github.sidky.data.repository.Tag
import com.github.sidky.photoscout.R
import com.github.sidky.photoscout.databinding.*
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxItemDecoration
import com.google.android.flexbox.FlexboxItemDecoration.BOTH
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

enum class InfoItemType {
    TITLE, OWNER, UPDATED_AT, DESCRIPTION, EXIF, TAGS, LOCATION
}

sealed class InfoItem(val itemType: InfoItemType) {
    data class Title(val title: String): InfoItem(InfoItemType.TITLE)
    data class Owner(val name: String): InfoItem(InfoItemType.OWNER)
    data class UpdatedAt(val date: Date): InfoItem(InfoItemType.UPDATED_AT)
    data class Description(val description: String): InfoItem(InfoItemType.DESCRIPTION)
    data class Exif(val label: String, val value: String): InfoItem(InfoItemType.EXIF)
    data class Tags(val tags: List<Tag>): InfoItem(InfoItemType.TAGS)
    data class Location(val latitude: Double, val longitude: Double): InfoItem(InfoItemType.LOCATION)
}

sealed class InfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    class TitleViewHolder(private val binding: ItemTitleBinding, private val onClick: () -> Unit) : InfoViewHolder(binding.root) {

        override fun apply(item: InfoItem) {
            when (item) {
                is InfoItem.Title -> {
                    binding.text = item.title
                    binding.handler = View.OnClickListener {
                        onClick()
                    }
                }
            }
        }
    }

    class OwnerViewHolder(private val binding: ItemOwnerBinding): InfoViewHolder(binding.root) {
        override fun apply(item: InfoItem) {
            when (item) {
                is InfoItem.Owner -> binding.text = item.name
            }
        }
    }

    class UpdatedAtViewHolder(private val binding: ItemUpdatedAtBinding): InfoViewHolder(binding.root) {

        private val formatter = DateFormat.getDateInstance(DateFormat.LONG)

        override fun apply(item: InfoItem) {
            when (item) {
                is InfoItem.UpdatedAt -> binding.updatedAt = formatter.format(item.date)
            }
        }
    }

    class DescriptionViewHolder(private val binding: ItemDescriptionBinding): InfoViewHolder(binding.root) {
        override fun apply(item: InfoItem) {
            when (item) {
                is InfoItem.Description -> binding.text = item.description
            }
        }
    }

    class ExifViewHolder(private val binding: ItemExifBinding): InfoViewHolder(binding.root) {
        override fun apply(item: InfoItem) {
            when (item) {
                is InfoItem.Exif -> {
                    binding.label = item.label
                    binding.value = item.value
                }
            }
        }
    }

    class TagsViewHolder(private val binding: ItemTagsBinding): InfoViewHolder(binding.root) {
        private val adapter: TagAdapter

        init {
            val context = binding.root.context
            adapter = TagAdapter()
            val lm = FlexboxLayoutManager(binding.root.context)
            lm.flexDirection = FlexDirection.ROW
            lm.justifyContent = JustifyContent.FLEX_START
            binding.tagsList.layoutManager = lm
            binding.tagsList.adapter = adapter
            val decoration = FlexboxItemDecoration(context)
            decoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.spacer))
            decoration.setOrientation(BOTH)
            binding.tagsList.addItemDecoration(decoration)
        }
        override fun apply(item: InfoItem) {
            when (item) {
                is InfoItem.Tags -> {
                    GlobalScope.launch {
                        adapter.update(item.tags)
                    }
                }
            }
        }
    }

    class LocationViewHolder(private val binding: ItemLocationBinding): InfoViewHolder(binding.root) {
        init {
            binding.map.onCreate(Bundle())
        }
        override fun apply(item: InfoItem) {
            when (item) {
                is InfoItem.Location -> {
                    binding.map.getMapAsync {
                        val ll = LatLng(item.latitude, item.longitude)
                        it.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 10.0f))

                        it.addMarker(MarkerOptions().position(ll))
                    }
                }
            }
        }

    }

    abstract fun apply(item: InfoItem)
}

class InfoAdapter : RecyclerView.Adapter<InfoViewHolder>() {

    val onBookmarked = MutableLiveData<Boolean>()

    private var infoItems: List<InfoItem> = emptyList()

    override fun getItemViewType(position: Int): Int = infoItems[position].itemType.ordinal

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        return when(InfoItemType.values()[viewType]) {
            InfoItemType.TITLE -> InfoViewHolder.TitleViewHolder(parent.inflate(R.layout.item_title)) {
                onBookmarked.postValue(true)
            }
            InfoItemType.OWNER -> InfoViewHolder.OwnerViewHolder(parent.inflate(R.layout.item_owner))
            InfoItemType.UPDATED_AT -> InfoViewHolder.UpdatedAtViewHolder(parent.inflate(R.layout.item_updated_at))
            InfoItemType.DESCRIPTION -> InfoViewHolder.DescriptionViewHolder(parent.inflate(R.layout.item_description))
            InfoItemType.EXIF -> InfoViewHolder.ExifViewHolder(parent.inflate(R.layout.item_exif))
            InfoItemType.TAGS -> InfoViewHolder.TagsViewHolder(parent.inflate(R.layout.item_tags))
            InfoItemType.LOCATION -> InfoViewHolder.LocationViewHolder(parent.inflate(R.layout.item_location))
        }
    }

    override fun getItemCount(): Int = infoItems.size

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        holder.apply(infoItems[position])
    }

    fun update(newItems: List<InfoItem>) {
        val r = DiffUtil.calculateDiff(InfoDiffCallback(infoItems, newItems))

        GlobalScope.launch(Dispatchers.Main) {
            infoItems = newItems
            r.dispatchUpdatesTo(this@InfoAdapter)
        }
    }

    fun update(detail: PhotoDetail) {
        val location = detail.location
        val locationItem: List<InfoItem.Location> = if (location != null) {
            listOf(InfoItem.Location(location.latitude, location.longitude))
        } else {
            emptyList()
        }
        val items = listOf(
            InfoItem.Title(detail.title),
            InfoItem.Owner(detail.owner.name),
            InfoItem.UpdatedAt(detail.uploadedAt),
            InfoItem.Description(detail.description)) +
                locationItem +
                listOf(InfoItem.Tags(detail.tags)) +
                detail.exifs.map { InfoItem.Exif(it.label, it.raw) }
        update(items)
    }
}

class InfoDiffCallback(private val oldList: List<InfoItem>, private val newList: List<InfoItem>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].itemType == newList[newItemPosition].itemType

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]
}