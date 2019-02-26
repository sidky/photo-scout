package com.github.sidky.data.koin

import androidx.room.Room
import com.apollographql.apollo.ApolloClient
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.github.sidky.data.dao.LoadingState
import com.github.sidky.data.dao.PhotoDatabase
import com.github.sidky.data.repository.PhotoRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val dataModule = module {
    single {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addNetworkInterceptor(StethoInterceptor())
            .build()
    }

    single(createOnStart = false) {
        ApolloClient.builder().serverUrl(get<String>("server")).okHttpClient(get()).build()
    }

    single(createOnStart = false) {
        Room.databaseBuilder(androidContext(), PhotoDatabase::class.java, "photo").build()
    }

    single(createOnStart = false) {
        get<PhotoDatabase>().dao()
    }

    single(createOnStart = false) {
        PhotoRepository(get(), get())
    }

    single {
        LoadingState(androidContext())
    }
}
