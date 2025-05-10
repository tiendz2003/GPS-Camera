pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = providers.gradleProperty("MAPBOX_API_KEY").get()
            }
        }
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.google.com")
        maven(url = "https://android-sdk.is.com/")
        maven(url = "https://maven.aliyun.com/repository/jcenter")
        maven(url = "https://artifact.bytedance.com/repository/pangle/")
        maven(url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        maven(url = "https://artifactory.bidmachine.io/bidmachine")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

rootProject.name = "GPS Camera - SSQUAD"
include(":app")
 