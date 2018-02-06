package com.github.sidky.photoscout.api.koin

import com.github.sidky.photoscout.api.FlickrApi
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import org.koin.dsl.module.applicationContext

val apiModule = applicationContext {
    factory {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    provide("flickr.apikey") {
        "6d7147d96585c88cd7795c303027c6e4"
    }

    provide {
        FlickrApi.service(get("flickr.apikey"), get())
    }
}