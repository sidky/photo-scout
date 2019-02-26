package com.github.sidky.photoscout.koin

import com.github.sidky.photoscout.PhotoListViewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val appModule = module {
    viewModel {
        PhotoListViewModel(get())
    }
}