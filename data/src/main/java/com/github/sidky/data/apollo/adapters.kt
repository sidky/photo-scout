package com.github.sidky.data.apollo

import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

val dateAdapter = object : CustomTypeAdapter<Date> {
    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    override fun encode(value: Date): CustomTypeValue<*> {
        return CustomTypeValue.GraphQLString(format.format(value))
    }

    override fun decode(value: CustomTypeValue<*>): Date {
        try {
            return format.parse(value.value.toString())
        } catch (e : ParseException) {
            throw RuntimeException(e)
        }
    }

}