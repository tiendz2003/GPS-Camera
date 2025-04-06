package com.example.baseproject.di

import com.example.baseproject.data.repository.CameraRepositoryImpl
import com.example.baseproject.data.repository.MediaRepositoryImpl
import com.example.baseproject.domain.CameraRepository
import com.example.baseproject.domain.MediaRepository
import com.example.baseproject.presentation.viewmodel.AlbumViewModel
import com.example.baseproject.presentation.viewmodel.PhotosViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppModule {
    val appModule = module {
        single<MediaRepository> { MediaRepositoryImpl(androidContext())}
        single<CameraRepository> { CameraRepositoryImpl()}
        viewModel { AlbumViewModel(get()) }
        viewModel { PhotosViewModel(get()) }
    }
}