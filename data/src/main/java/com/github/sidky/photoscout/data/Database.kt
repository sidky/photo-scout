package com.github.sidky.photoscout.data

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import timber.log.Timber

@Entity(tableName = "photo", indices = [Index(value = ["photo_id"], unique = true)])
data class Photo(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "photo_id") var photoId: String,
    @ColumnInfo(name = "owner_name") var ownerName: String,
    @Embedded var location: Location?)

@Entity(
    foreignKeys = arrayOf(ForeignKey(
        entity = Photo::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("photo_id"),
        onDelete = ForeignKey.CASCADE
    )),
    tableName = "photo_url"
)
data class SizedURL(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "photo_id") var photoId: Long,
    @ColumnInfo(name = "url") var url: String,
    @ColumnInfo(name = "width") var width: Int,
    @ColumnInfo(name = "height") var height: Int)

data class Location(var longitude: Double, var latitude: Double, var accuracy: Int)

data class PhotoWithURL(
    @Embedded val photo: Photo,

    @Relation(parentColumn = "id", entityColumn = "photo_id", entity = SizedURL::class)
    val urls: List<SizedURL>
)

@Dao
interface PhotoDAO {

    @Query("SELECT * FROM photo")
    fun photos(): DataSource.Factory<Int, PhotoWithURL>

    @Query("SELECT * FROM photo")
    fun photosList(): List<PhotoWithURL>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllPhotos(photos: List<Photo>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllURLs(urls: List<SizedURL>)

    @Query("DELETE FROM photo")
    fun deleteAll()

    @Transaction
    fun setPhotos(photos: List<PhotoWithURL>) {
        deleteAll()
        val ids = insertAllPhotos(photos.map { it.photo })

        val urls = ids.zip(photos).flatMap {
            val id = it.first
            val photo = it.second

            photo.urls.map { it.copy(photoId = id) }
        }

        Timber.e("URLS: ${urls}")

        insertAllURLs(urls)
    }

    @Transaction
    fun addPhotos(photos: List<PhotoWithURL>) {
        val ids = insertAllPhotos(photos.map { it.photo })

        val urls = ids.zip(photos).flatMap {
            val id = it.first
            val photo = it.second

            photo.urls.map { it.copy(photoId = id) }
        }

        insertAllURLs(urls)
    }
}

@Database(entities = arrayOf(Photo::class, SizedURL::class), version = 1)
abstract class PhotoDatabase : RoomDatabase() {
    abstract fun dao(): PhotoDAO
}