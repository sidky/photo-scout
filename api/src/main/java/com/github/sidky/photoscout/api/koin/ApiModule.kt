package com.github.sidky.photoscout.api.koin

import com.github.sidky.photoscout.api.BuildConfig
import com.github.sidky.photoscout.api.FlickrApi
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import org.koin.dsl.module.applicationContext

val apiModule = applicationContext {
    factory {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    provide("flickr.apikey") {
        BuildConfig.API_KEY_FLICKR
    }

    provide {
        FlickrApi.service(apiKey = get("flickr.apikey"), moshi = get())
    }
}