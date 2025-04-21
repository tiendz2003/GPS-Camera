package com.example.baseproject.di

import com.example.baseproject.data.networking.WeatherApiService
import com.example.baseproject.data.repository.CameraRepositoryImpl
import com.example.baseproject.data.repository.MapLocationRepositoryImpl
import com.example.baseproject.data.repository.MediaRepositoryImpl
import com.example.baseproject.data.repository.WeatherRepositoryImpl
import com.example.baseproject.domain.CameraRepository
import com.example.baseproject.domain.MapLocationRepository
import com.example.baseproject.domain.MediaRepository
import com.example.baseproject.domain.WeatherRepository
import com.example.baseproject.presentation.viewmodel.AlbumViewModel
import com.example.baseproject.presentation.viewmodel.CameraViewModel
import com.example.baseproject.presentation.viewmodel.MapSettingViewModel
import com.example.baseproject.presentation.viewmodel.PhotosViewModel
import com.example.baseproject.presentation.viewmodel.PreviewShareViewModel
import com.example.baseproject.worker.CacheDataTemplate
import com.example.baseproject.worker.LoadDataTemplateWorker
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppModule {
    private val cacheModule = module {
        single { CacheDataTemplate() }
        factory { LoadDataTemplateWorker.Factory(get(), get(), get()) }
    }
    val appModule = module {
        single<MediaRepository> { MediaRepositoryImpl(androidContext()) }
        single<CameraRepository> { CameraRepositoryImpl() }
        single<MapLocationRepository> { MapLocationRepositoryImpl(androidContext()) }
        single<WeatherRepository> { WeatherRepositoryImpl(get()) }
        single { provideRetrofit() }
        single { provideWeatherApiService(get()) }
        viewModel { AlbumViewModel(get()) }
        viewModel { PhotosViewModel(get(),get()) }
        viewModel { CameraViewModel(get(), get(),get(),get(),get()) }
        viewModel { PreviewShareViewModel(get()) }
        viewModel { MapSettingViewModel(get(),get()) }
        includes(cacheModule)
    }

    private fun provideRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://wttr.in/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideWeatherApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }
}