package com.ssquad.gps.camera.geotag.di

import androidx.room.Room
import com.ssquad.gps.camera.geotag.data.local.db.AppDatabase
import org.koin.dsl.module

object DatabaseModule{
    val databaseModule = module {
        single {
            Room.databaseBuilder(
                get(),
                AppDatabase::class.java,
                "photos"
            ).build()
        }
        single {
            get<AppDatabase>().photoDao()
        }
    }


}