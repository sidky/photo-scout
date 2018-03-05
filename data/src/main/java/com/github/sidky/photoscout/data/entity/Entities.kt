package com.github.sidky.photoscout.data.entity

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE

data class Location(val latitude: Double,
                    val longitude: Double,
                    val accuracy: Int)

@Entity(tableName = "photo")
data class Photo(@PrimaryKey val id: Long,
                 @ColumnInfo(name = "order_id") val order: Int,
                 @ColumnInfo(name = "title") val title: String,
                 @Embedded val location: Location?)
@Entity(tableName = "photo_sizes",
        foreignKeys = [
            ForeignKey(entity = Photo::class,
            parentColumns = ["id"],
            childColumns = ["photo_id"],
            onDelete = CASCADE)])
data class PhotoSize(@PrimaryKey(autoGenerate = true) val id: Long = 0L,
                     @ColumnInfo(name = "photo_id") val photoId: Long,
                     val url: String,
                     val width: Int,
                     val height: Int)

data class PhotoWithSize(val id: Long,
                         @ColumnInfo(name = "order_id") val order: Int,
                         @ColumnInfo(name = "title") val title: String,
                         @Embedded val location: Location?) {

    @Relation(parentColumn = "id", entityColumn = "photo_id")
    var sizes: List<PhotoSize> = emptyList()

    fun largestImage(maxDimension: Int): PhotoSize? =
        sizes.filter { it.width <= maxDimension && it.height <= maxDimension }
                .maxBy { it.width * it.height } ?: smallestImage()

    fun smallestImage(minDimension: Int): PhotoSize? =
            sizes.filter { it.width >= minDimension || it.height >= minDimension }
                    .minBy { it.width * it.height } ?: largestImage()

    fun smallestImage(): PhotoSize? =
        sizes.minBy { it.width * it.height } ?: sizes.firstOrNull()

    fun largestImage(): PhotoSize? =
            sizes.maxBy { it.width * it.height }
}
