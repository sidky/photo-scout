package com.github.sidky.photoscout.data.dao

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import com.github.sidky.photoscout.data.entity.Photo
import com.github.sidky.photoscout.data.entity.PhotoSize

@Database(entities = [Photo::class, PhotoSize::class], version = 2)
abstract class PhotoDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE photo ADD COLUMN order_id INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}

