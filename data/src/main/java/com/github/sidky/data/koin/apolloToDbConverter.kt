package com.github.sidky.data.koin

import com.github.sidky.data.converter.*
import org.koin.dsl.module.module

val apolloToDBConverterModule = module {
    single { ApolloLocationToDBConverter() }
    single { ApolloPhotoUrlToDBConverter() }
    single { PhotoUrlListConverter(get()) }
    single { ApolloPhotoToDBPhotoConverter(get()) }
    single { ApolloPhotoToDBPhotoWithURLConverter(get(), get()) }
    single { ApolloPhotoListToDBConverter(get()) }
}