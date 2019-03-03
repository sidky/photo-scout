package com.github.sidky.photoscout.koin

import com.github.sidky.common.Converter
import com.github.sidky.data.paging.BoundingBox
import com.github.sidky.photoscout.PhotoListViewModel
import com.github.sidky.photoscout.converter.BoundingBoxToLatLngBoundConverter
import com.github.sidky.photoscout.converter.LatLngBoundToBoundingBoxConverter
import com.google.android.gms.maps.model.LatLngBounds
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val appModule = module {
    viewModel {
        PhotoListViewModel(get())
    }

    single<Converter<BoundingBox, LatLngBounds>> {
        LatLngBoundToBoundingBoxConverter()
    }

    single {
        BoundingBoxToLatLngBoundConverter()
    }
}