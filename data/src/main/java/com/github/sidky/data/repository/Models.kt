package com.github.sidky.data.repository

import java.util.*

data class Owner(val name: String, val location: String?)

data class Tag(val tag: String, val isMachineTag: Boolean)

data class Exif(val label: String, val raw: String)

data class Location(val latitude: Double, val longitude: Double, val accuracy: Int)

data class PhotoDetail(
    val id: String,
    val uploadedAt: Date,
    val owner: Owner,
    val title: String,
    val description: String,
    val camera: String?,
    val tags: List<Tag>,
    val exifs: List<Exif>,
    val location: Location?,
    val bookmarked: Boolean)