import java.text.SimpleDateFormat
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    id ("kotlin-parcelize")
}

android {
    namespace = "com.ssquad.gps.camera.geotag"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ssquad.gps.camera.geotag"
        minSdk = 29
        targetSdk = 35
//        versionCode = 100
//        versionName = "1.0.0"

        versionCode = 1
        versionName = "test"

        val dateTime = SimpleDateFormat("dd-MM-yyyy").format(System.currentTimeMillis())
        val archivesName = "Base - Project ($versionCode)_$dateTime"
        extensions.getByType(BasePluginExtension::class.java).archivesName.set(archivesName)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            val properties = Properties().apply {
                load(rootProject.file("local.properties").inputStream())
            }
            val apiKey = checkNotNull(properties.getProperty("MAP_API_KEY")){
                "404:NOT FOUND"
            }
            resValue("string", "google_maps_key", apiKey)
            buildConfigField(
                "String",
                "MAP_API_KEY",
                "\"$apiKey\""
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.mapbox.search:autofill:2.12.0-beta.1")
    implementation("com.mapbox.search:place-autocomplete:2.12.0-beta.1")
    implementation("com.mapbox.search:mapbox-search-android:2.12.0-beta.1")
    implementation("com.mapbox.maps:android:11.9.0")
    implementation ("androidx.lifecycle:lifecycle-service:2.8.1")
    implementation("com.google.guava:guava:31.1-android")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.arthenica:ffmpeg-kit-min-gpl:6.0-2")
    implementation ("androidx.work:work-runtime-ktx:2.10.0")
    implementation (libs.androidx.camera.core)
    implementation ("androidx.camera:camera-camera2:1.4.2")
    implementation ("com.faltenreich:skeletonlayout:4.0.0")
    implementation("androidx.camera:camera-effects:1.4.2")
    implementation (libs.androidx.camera.lifecycle)
    implementation (libs.androidx.camera.view)
    implementation (libs.androidx.camera.extensions)
    implementation(libs.play.services.ads)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.config.ktx)
    implementation (libs.androidx.camera.video)
    implementation(libs.koin.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.google.android.exoplayer:exoplayer:2.19.1") // Thay X.X bằng phiên bản mới nhất


    //ads library
    implementation(libs.ssquadadslibrary)

    //other library
    implementation(libs.hawk)
    implementation(libs.lottie)
    implementation(libs.dotsindicator)
    implementation(libs.glide)
    implementation(libs.user.messaging.platform)
    implementation(libs.gson)
}