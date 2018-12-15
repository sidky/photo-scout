package com.github.sidky.photoscout

import com.github.sidky.photoscout.viewmodel.ActionBarViewModel
import com.github.sidky.photoscout.viewmodel.PhotoDetailViewModel
import com.github.sidky.photoscout.viewmodel.PhotoMapViewModel
import com.github.sidky.photoscout.viewmodel.PhotoViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

val appModule = module {
    viewModel {
        PhotoViewModel(get())
    }

    viewModel {
        PhotoMapViewModel(get())
    }

    viewModel {
        PhotoDetailViewModel(get())
    }

    viewModel {
        ActionBarViewModel()
    }

    single<CoroutineDispatcher>("background") {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }

    single<CoroutineDispatcher>("foreground") {
        Dispatchers.Main
    }
}