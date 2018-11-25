package com.github.sidky.photoscout

import android.app.Application
import com.facebook.stetho.Stetho
import com.github.sidky.photoscout.data.dataModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class PhotoScoutApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initializeKoin()
        initializeTimber()
        initializeStetho()
    }

    fun initializeKoin() {
        startKoin(this, listOf(appModule, dataModule))
    }

    fun initializeTimber() {
        Timber.plant(Timber.DebugTree())
    }

    fun initializeStetho() {
        Stetho.initializeWithDefaults(this)
    }
}