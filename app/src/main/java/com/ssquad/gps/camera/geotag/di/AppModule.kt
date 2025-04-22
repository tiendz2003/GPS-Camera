package com.ssquad.gps.camera.geotag.di

import com.ssquad.gps.camera.geotag.data.networking.WeatherApiService
import com.ssquad.gps.camera.geotag.data.repository.CameraRepositoryImpl
import com.ssquad.gps.camera.geotag.data.repository.MapLocationRepositoryImpl
import com.ssquad.gps.camera.geotag.data.repository.MediaRepositoryImpl
import com.ssquad.gps.camera.geotag.data.repository.WeatherRepositoryImpl
import com.ssquad.gps.camera.geotag.domain.CameraRepository
import com.ssquad.gps.camera.geotag.domain.MapLocationRepository
import com.ssquad.gps.camera.geotag.domain.MediaRepository
import com.ssquad.gps.camera.geotag.domain.WeatherRepository
import com.ssquad.gps.camera.geotag.presentation.viewmodel.AlbumViewModel
import com.ssquad.gps.camera.geotag.presentation.viewmodel.CameraViewModel
import com.ssquad.gps.camera.geotag.presentation.viewmodel.MapSettingViewModel
import com.ssquad.gps.camera.geotag.presentation.viewmodel.PhotosViewModel
import com.ssquad.gps.camera.geotag.presentation.viewmodel.PreviewShareViewModel
import com.ssquad.gps.camera.geotag.worker.CacheDataTemplate
import com.ssquad.gps.camera.geotag.worker.LoadDataTemplateWorker
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

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
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
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