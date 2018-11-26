package com.github.sidky.photoscout.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.apollographql.apollo.ApolloClient
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.github.sidky.photoscout.data.adapter.ApolloAdapters
import com.github.sidky.photoscout.graphql.InterestingQuery
import com.github.sidky.photoscout.graphql.type.CustomType
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.module
import timber.log.Timber

val dataModule = module {
    single<PhotoDatabase> {
        Room.databaseBuilder(get(), PhotoDatabase::class.java, "photos")
            .fallbackToDestructiveMigration()
            .build()
    }

    single<PhotoDAO> {
        get<PhotoDatabase>().dao()
    }

    single<Interceptor>("logging") {
        HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
            Timber.tag("OkHttp").d(message)
        })
    }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(get("logging"))
            .addNetworkInterceptor(StethoInterceptor())
            .build()
    }

    single<ApolloClient> {
        ApolloClient.builder()
            .serverUrl("https://immense-tor-66837.herokuapp.com/graphql")
            .okHttpClient(get())
            .addCustomTypeAdapter(CustomType.TIME, ApolloAdapters.dateAdapter)
            .build()
    }

    single<PhotoRepository> {
        PhotoRepository(get(), get())
    }
}