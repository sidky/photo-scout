package com.github.sidky.photoscout.data

import android.arch.persistence.room.Room
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.github.sidky.photoscout.data.dao.PhotoDao
import com.github.sidky.photoscout.data.dao.PhotoDatabase
import com.github.sidky.photoscout.data.repository.FlickrPhotoRepository
import com.github.sidky.photoscout.data.repository.RequestQueue
import io.reactivex.schedulers.Schedulers
import org.koin.dsl.module.applicationContext
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

val dataModule = applicationContext {
    provide {
        Room.databaseBuilder(get<Context>(), PhotoDatabase::class.java, "photo.db")
                .addMigrations(PhotoDatabase.MIGRATION_1_2)
                .build() as PhotoDatabase
    } bind PhotoDatabase::class

    provide {
        get<PhotoDatabase>().photoDao()
    } bind PhotoDao::class

    provide("scheduler.io") {
        Schedulers.io()
    }

    bean {
        RequestQueue(scheduler = get("scheduler.io"))
    }

    provide {
        FlickrPhotoRepository(
                dao = get(),
                flickrApi = get(),
                requestQueue = get(),
                mainThreadExecutor = get("executor.main"),
                backgroundThreadExecutor = get("executor.background"))
    }

    provide("handler.main") {
        Handler(Looper.getMainLooper())
    }

    provide("executor.main") {
        Executor {
            (get("handler.main") as Handler).post(it)
        }
    }

    provide("executor.background") {
        Executors.newSingleThreadExecutor()
    }
}