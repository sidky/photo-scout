package com.github.sidky.photoscout

import com.github.sidky.photoscout.data.repository.FlickrPhotoRepository
import com.github.sidky.photoscout.details.PhotoDetailsPresenter
import org.koin.dsl.module.applicationContext

val appModule = applicationContext {

    context("photo.list") {
        bean {
            PhotoPresenter(get(), get())
        }
    }

    context("photo.details") {
        bean {
            PhotoDetailsPresenter(flickrRepository = get())
        }
    }
}