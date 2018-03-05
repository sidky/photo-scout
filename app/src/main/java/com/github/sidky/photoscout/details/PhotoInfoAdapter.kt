package com.github.sidky.photoscout.details

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.sidky.photoscout.R

abstract class AbstractInfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: PhotoDetailItem)
}

enum class InfoItemType {
    TITLE, OWNER, DESCRIPTION
}

class TextInfoViewHolder(val view: TextView): AbstractInfoViewHolder(view) {
    override fun bind(item: PhotoDetailItem) {
        when (item) {
            is PhotoDetailItem.Title -> view.text = item.title
            is PhotoDetailItem.Owner ->
                view.text = view.context.getString(R.string.by_author, item.ownerName)
            is PhotoDetailItem.Description -> view.text = item.description
            else -> Log.e("Info", "Invalid item: ${item} for text info view holder")
        }
    }
}

class PhotoInfoAdapter : RecyclerView.Adapter<AbstractInfoViewHolder>() {
    override fun getItemViewType(position: Int): Int =
        when (items.get(position)) {
            is PhotoDetailItem.Title -> InfoItemType.TITLE
            is PhotoDetailItem.Description -> InfoItemType.DESCRIPTION
            is PhotoDetailItem.Owner -> InfoItemType.OWNER
            else -> null
        }?.ordinal ?: -1

    var items: List<PhotoDetailItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractInfoViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        return when (InfoItemType.values()[viewType]) {
            InfoItemType.TITLE -> {
                val v = inflator.inflate(R.layout.info_title, parent, false) as TextView
                TextInfoViewHolder(v)
            }
            InfoItemType.OWNER -> {
                val v = inflator.inflate(R.layout.info_owner, parent, false) as TextView
                TextInfoViewHolder(v)
            }
            InfoItemType.DESCRIPTION -> {
                val v = inflator.inflate(R.layout.info_description, parent, false) as TextView
                TextInfoViewHolder(v)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AbstractInfoViewHolder, position: Int) {
        holder.bind(items.get(position))
    }


}