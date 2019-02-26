package com.github.sidky.common

interface Converter<R, T> {
    fun convert(t: T): R

    fun convertNullable(t: T?): R? {
        if (t == null) {
            return null
        } else {
            return convert(t)
        }
    }
}

open class ListConverter<R, T>(val converter: Converter<R, T>): Converter<List<R>, List<T>> {
    override fun convert(t: List<T>): List<R> {
        return t.map { converter.convert(it) }
    }

    fun convertNullableList(t: List<T?>): List<R?> {
        return t.map { converter.convertNullable(it) }
    }
}