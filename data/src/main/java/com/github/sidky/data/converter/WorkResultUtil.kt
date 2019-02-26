package com.github.sidky.data.converter

import androidx.work.Data
import com.github.sidky.photoscout.graphql.fragment.NextPage

data class Pagination(val success: Boolean, val hasNext: Boolean, val next: Int?)

object WorkResultUtil {
    fun success(nextPage: NextPage?): Data {
        return Data.Builder()
            .putBoolean("success", true)
            .putBoolean("hasNext", nextPage?.hasNext() ?: false)
            .putNullableInt("next", nextPage?.next())
            .build()
    }

    fun inputPage(page: Int): Data = Data.Builder().putInt("page", page).build()

    fun query(query: String, page: Int = 0): Data = Data.Builder().putString("query", query).putInt("page", page).build()

    fun parse(data: Data): Pagination {
        val m = data.keyValueMap

        val next = if (m.containsKey("next")) data.getInt("next", 0) else null
        return Pagination(
            data.getBoolean("success", false),
            data.getBoolean("hasNext", false),
            next)
    }

    fun Data.Builder.putNullableInt(key: String, value: Int?): Data.Builder {
        if (value != null) {
            putInt(key, value)
        }
        return this
    }
}