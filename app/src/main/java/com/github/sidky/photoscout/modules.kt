package com.github.sidky.photoscout

import com.github.sidky.photoscout.viewmodel.ActionBarViewModel
import com.github.sidky.photoscout.viewmodel.PhotoDetailViewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val appModule = module {
    viewModel {
        PhotoViewModel(get())
    }

    viewModel {
        PhotoDetailViewModel(get())
    }

    viewModel {
        ActionBarViewModel()
    }
}