package com.github.sidky.photoscout

import java.text.DateFormat
import java.text.DateFormat.LONG
import java.util.*

object DisplayDateFormatter {
    private val formatter = DateFormat.getDateInstance(LONG)

    @JvmStatic
    fun format(date: Date): String {
        return formatter.format(date)
    }
}