package com.github.sidky.photoscout.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.AndroidJUnitRunner
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoDAOTest {
    private lateinit var dao: PhotoDAO
    private lateinit var db: PhotoDatabase

    @Before
    fun createDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PhotoDatabase::class.java).build()
        dao = db.dao()
    }

    @After
    fun closeDB() {
        db.close()
    }

    @Test
    fun testInsertPhotoAndURL() {
//        val photos = listOf(,
//            Photo(2, "photo2", "owner2", null),
//            Photo(3, "photo3", "owner3", null))

        val photos = listOf(
            PhotoWithURL(Photo(1, "photo1", "owner1", null),
                listOf(SizedURL(null, 0, "url1", 100, 150))),
            PhotoWithURL(Photo(2, "photo2", "owner2", null),
                listOf(SizedURL(null, 0, "url2", 200, 300))),
            PhotoWithURL(Photo(3, "photo3", "owner3", null),
                listOf(SizedURL(null, 0, "url4", 100, 300),
                    SizedURL(null, 0, "url5", 200, 300),
                    SizedURL(null, 0, "url6", 500, 300)))
            )

        dao.setPhotos(photos)

        val list = dao.photosList().sortedBy { it.photo.id }
        assertEquals(3, list.size)

        assertEquals("photo1", list[0].photo.photoId)
        assertEquals(photos[0].urls, list[0].urls)
    }
}