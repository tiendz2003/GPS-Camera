import java.text.SimpleDateFormat
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("kotlin-parcelize")
}

android {
    namespace = "com.example.baseproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.baseproject"
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
        /*debug {
            val properties = Properties().apply {
                load(rootProject.file("local.properties").inputStream())
            }
            val apiKey = checkNotNull(properties.getProperty("WEATHER_API_KEY")){
                "Không tìm thấy API_KEY trong local.properties"
            }
            buildConfigField(
                "String",
                "WEATHER_API_KEY",
                "\"$apiKey\""
            )
        }*/
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
    implementation("com.google.guava:guava:31.1-android")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    // CameraX Core (Bắt buộc)
    implementation (libs.androidx.camera.core)
    implementation ("androidx.camera:camera-camera2:1.4.2")
    // CameraX Lifecycle để tự động quản lý camera theo lifecycle
    implementation (libs.androidx.camera.lifecycle)
    // CameraX View để hiển thị preview
    implementation (libs.androidx.camera.view)
    // CameraX Extensions (Tùy chọn, dùng để có chế độ HDR, Night Mode...)
    implementation (libs.androidx.camera.extensions)
    // CameraX VideoCapture (Nếu muốn quay video)
    implementation (libs.androidx.camera.video)
    implementation(libs.koin.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //ads library
    implementation(libs.ssquadadslibrary)

    //other library
    implementation(libs.hawk)
    implementation(libs.lottie)
    implementation(libs.dotsindicator)
    implementation(libs.glide)
    implementation(libs.user.messaging.platform)

}