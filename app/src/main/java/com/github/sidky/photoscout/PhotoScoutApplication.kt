package com.github.sidky.photoscout

import android.app.Application
import com.github.sidky.photoscout.api.koin.apiModule
import org.koin.android.ext.android.startKoin

class PhotoScoutApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(apiModule))
    }
}