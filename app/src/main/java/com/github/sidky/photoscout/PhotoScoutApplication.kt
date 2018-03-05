package com.github.sidky.photoscout

import android.app.Application
import com.facebook.stetho.Stetho
import com.github.sidky.photoscout.api.koin.apiModule
import com.github.sidky.photoscout.data.dataModule
import com.github.sidky.photoscout.data.repository.RequestQueue
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class PhotoScoutApplication : Application() {

    val requestQueue: RequestQueue by inject<RequestQueue>()

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        startKoin(this, listOf(apiModule, dataModule, appModule))

        Stetho.initializeWithDefaults(this)
        requestQueue.start()

    }
}