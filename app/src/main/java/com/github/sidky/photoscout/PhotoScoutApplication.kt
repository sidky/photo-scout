package com.github.sidky.photoscout

import android.app.Application
import com.facebook.stetho.Stetho
import com.github.sidky.common.koin.constantsModule
import com.github.sidky.data.koin.apolloToDBConverterModule
import com.github.sidky.data.koin.dataModule
import com.github.sidky.photoscout.koin.appModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.android.startKoin
import timber.log.Timber

open class PhotoScoutApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(dataModule, constantsModule, apolloToDBConverterModule, appModule))

        FirebaseApp.initializeApp(this)

        initializeTimber()
        initializeStetho()
    }

    private fun initializeTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun initializeStetho() {
        Stetho.initializeWithDefaults(this)
    }
}