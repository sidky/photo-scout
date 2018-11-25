package com.github.sidky.photoscout

import org.koin.dsl.module.module

val appModule = module {
    single<PhotoPresenter> {
        PhotoPresenter(get())
    }
}