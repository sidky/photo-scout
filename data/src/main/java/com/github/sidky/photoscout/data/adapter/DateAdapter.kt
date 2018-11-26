package com.github.sidky.photoscout.data.adapter

import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object ApolloAdapters {
    val dateAdapter = object : CustomTypeAdapter<Date> {

        // 2018-11-24T17:38:04Z
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

        override fun encode(value: Date): CustomTypeValue<*> {
            return CustomTypeValue.GraphQLString(dateFormat.format(value))
        }

        override fun decode(value: CustomTypeValue<*>): Date {
            return dateFormat.parse(value.value.toString())
        }

    }
}