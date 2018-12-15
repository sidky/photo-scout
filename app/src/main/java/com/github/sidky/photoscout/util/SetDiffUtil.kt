package com.github.sidky.photoscout.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

interface KeyExtractor<T> {
    fun key(t: T): String
}

interface DiffCallback<T> {
    fun add(item: T)
    fun update(oldItem: T, newItem: T)
    fun remove(item: T)
}

class SetDiffUtil<T : Any>(private val extractor: KeyExtractor<T>, private val callback: DiffCallback<T>) {
    private var items: Map<String, T> = mapOf()

    private val mutex = Mutex(false)

    suspend fun update(newItems: Collection<T>) = runBlocking {

        mutex.withLock {
            val newItemMap = newItems.map {
                Pair(extractor.key(it), it)
            }.toMap()
            val allKeys = (items.keys + newItemMap.keys)

            val diffs = allKeys.map {
                val oldItem = items[it]
                val newItem = newItemMap[it]

                if (oldItem == null && newItem != null) {
                    Diff.Add(newItem)
                } else if (oldItem != null && newItem == null) {
                    Diff.Delete(oldItem)
                } else if (oldItem != null && newItem != null) {
                    Diff.Update(oldItem, newItem)
                } else {
                    null
                }
            }.filterNotNull()

            items = newItemMap

            Timber.e("Diff size: %d", diffs.size)

            diffs.forEach { diff ->
                Timber.e("Inside diff")
                launch(Dispatchers.Main) {
                    Timber.e("Update: %s", diff)
                    when (diff) {
                        is Diff.Add<T> -> callback.add(diff.item)
                        is Diff.Delete<T> -> callback.remove(diff.item)
                        is Diff.Update<T> -> callback.update(diff.oldItem, diff.newItem)
                    }
                }
            }
        }
    }

    private sealed class Diff<T> {
        data class Add<T>(val item: T): Diff<T>()
        data class Delete<T>(val item: T): Diff<T>()
        data class Update<T>(val oldItem: T, val newItem: T): Diff<T>()
    }
}