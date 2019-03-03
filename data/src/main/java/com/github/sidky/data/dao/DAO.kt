package com.github.sidky.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import kotlinx.coroutines.runBlocking

data class PhotoThumbnail(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "photo_id") val photoId: Long,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "width") val width: Int,
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "pixels") val pixels: Int)

data class ThumbnailWithLocation(
    @ColumnInfo(name = "id") val id: Long,
    @Embedded val location: Location,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "width") val width: Int,
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "pixels") val pixels: Int)

@Dao
interface PhotoDAO {

    @Transaction @Query("SELECT * FROM photo WHERE id = :id")
    suspend fun getPhoto(id: Long): PhotoWithURL

    @Query("SELECT * FROM photo")
    suspend fun allPhotos(): List<Photo>

    @Transaction
    @Query("""
        SELECT id, longitude, latitude, accuracy, url, width, height, min(pixels) AS pixels from (
            SELECT
                photo.id AS id,
                photo.longitude AS longitude,
                photo.latitude AS latitude,
                photo.accuracy AS accuracy,
                photo_url.url AS url,
                photo_url.width AS width,
                photo_url.height AS height,
                (photo_url.width * photo_url.height) AS pixels
            FROM photo INNER JOIN photo_url ON photo.id == photo_url.photo_id
            WHERE
                latitude IS NOT NULL AND
                longitude IS NOT NULL AND
                accuracy IS NOT NULL AND
                width >= :dimension AND
                height >= :dimension)
        GROUP BY id
        """)
    fun allPhotoWithLocation(dimension: Int): LiveData<List<ThumbnailWithLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<Photo>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertURLs(urls: List<PhotoURL>)

    @Query("DELETE FROM photo")
    suspend fun deleteAll()

    @Transaction @Query("SELECT * FROM photo ORDER BY id")
    fun allPhotosWithURLs(): DataSource.Factory<Int, PhotoWithURL>

    @Query("SELECT id, photo_id, url, width, height, min(width * height) as pixels FROM photo_url WHERE width >= :dimension AND height >= :dimension GROUP BY photo_id ORDER BY id ASC")
    fun thumbnails(dimension: Int): DataSource.Factory<Int, PhotoThumbnail>

    @Transaction
    fun insertPhotoWithURLS(photos: List<PhotoWithURL>) {
        val p = photos.map { it.photo }

        runBlocking {
            val ids = insertPhotos(p)
            val u = ids.zip(photos).map {
                val (id, photo) = it
                photo.urls.map { it.copy(photoId = id) }
            }.flatten()
            insertURLs(u)
        }
    }

    @Transaction
    fun setPhotsWithURLs(photos: List<PhotoWithURL>) {
        runBlocking {
            deleteAll()
            insertPhotoWithURLS(photos)
        }
    }
}

@Database(version = 1, entities = [Photo::class, PhotoURL::class])
abstract class PhotoDatabase : RoomDatabase() {
    abstract fun dao(): PhotoDAO
}