package com.github.sidky.photoscout.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.sidky.data.repository.Tag
import com.github.sidky.photoscout.R
import com.github.sidky.photoscout.databinding.ItemSingleTagBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TagViewHolder(private val binding: ItemSingleTagBinding): RecyclerView.ViewHolder(binding.root) {
    fun apply(tag: Tag) {
        binding.name = tag.tag
        binding.executePendingBindings()
    }
}

class TagAdapter : RecyclerView.Adapter<TagViewHolder>() {
    private var tags: List<Tag> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder =
        TagViewHolder(parent.inflate(R.layout.item_single_tag))

    override fun getItemCount(): Int = tags.size

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) =
            holder.apply(tags[position])

    suspend fun update(newTags: List<Tag>) {
        val r = DiffUtil.calculateDiff(TagDiffCallback(tags, newTags))

        GlobalScope.launch(Dispatchers.Main) {
            tags = newTags
            r.dispatchUpdatesTo(this@TagAdapter)
        }
    }

    class TagDiffCallback(private val oldTags: List<Tag>, private val newTags: List<Tag>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldTags[oldItemPosition] == newTags[newItemPosition]

        override fun getOldListSize(): Int = oldTags.size

        override fun getNewListSize(): Int = newTags.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldTags[oldItemPosition] == newTags[newItemPosition]
    }
}
