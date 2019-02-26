package com.github.sidky.data.dao

import androidx.room.*

@Entity(tableName = "photo")
data class Photo(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "photo_id") var photoId: String,
    @ColumnInfo(name = "owner") var owner: String,
    @Embedded var location: Location?)


data class Location(
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @ColumnInfo(name = "accuracy") var accuracy: Int)

@Entity(
    tableName = "photo_url",
    foreignKeys = [ForeignKey(entity = Photo::class, parentColumns = ["id"], childColumns = ["photo_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("photo_id")]
)
data class PhotoURL(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "photo_id") var photoId: Long,
    @ColumnInfo(name = "url") var url: String,
    @ColumnInfo(name = "width") var width: Int,
    @ColumnInfo(name = "height") var height: Int)

data class PhotoWithURL(
    @Embedded val photo: Photo,

    @Relation(parentColumn = "id", entityColumn = "photo_id", entity = PhotoURL::class)
    val urls: List<PhotoURL>)
