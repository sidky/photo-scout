package com.github.sidky.photoscout

import com.github.sidky.photoscout.details.PhotoDetailsPresenter
import org.koin.dsl.module.applicationContext

val appModule = applicationContext {
    provide {
        PhotoPresenter(get())
    }

    provide {
        PhotoDetailsPresenter(flickrRepository = get())
    }
}