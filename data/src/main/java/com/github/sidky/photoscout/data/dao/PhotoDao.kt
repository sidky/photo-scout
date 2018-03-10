package com.github.sidky.photoscout.data.dao

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.paging.PagedList
import android.arch.persistence.room.*
import com.github.sidky.photoscout.data.entity.Photo
import com.github.sidky.photoscout.data.entity.PhotoSize
import com.github.sidky.photoscout.data.entity.PhotoWithSize

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photo ORDER BY order_id")
    @Transaction
    fun photos(): DataSource.Factory<Int, PhotoWithSize>

    @Query("DELETE FROM photo")
    fun clearPhotos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPhotos(photo: List<Photo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPhotoSizes(photoSizes: List<PhotoSize>)

    @Transaction
    fun addPhotosWithSizes(photos: List<Photo>, sizes: List<PhotoSize>, clear: Boolean = false) {
        if (clear) {
            clearPhotos()
        }
        addPhotos(photos)
        addPhotoSizes(sizes)
    }
}