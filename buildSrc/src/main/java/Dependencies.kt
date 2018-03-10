object Versions {
    val compileSdkVersion = 27
    val minSdkVersion = 21
    val targetSdkVersion = 27
    val kotlinVersion = "1.2.21"
    val gradleVersion = "3.2.0-alpha05"

    val koinVersion = "0.9.0"
    val liveDataVersion = "1.1.0"
    val moshiVersion = "1.5.0"
    val retrofitVersion = "2.3.0"
    val roomVersion = "1.1.0-alpha3"
    val rxJavaVersion = "2.1.10"
    val rxKotlinVersion = "2.2.0"
    val rxAndroidVersion = "2.0.2"
    val pagingVersion = "1.0.0-alpha6"
    val stethoVersion = "1.5.0"
    val supportLibraryVersion = "27.1.0"

    val glideVersion = "4.6.1"

    val kotlinxVersion = "0.22.3"

    val timberVersion = "4.6.1"

    val flexBoxVersion = "0.3.2"

    val androidKtxVersion = "0.2"

    val playServicesVersion = "11.8.0"
    val constraintLayoutVersion = "1.0.2"

    val mapUtilVersion = "0.5"
}

object Deps {
    val gradlePlugin = "com.android.tools.build:gradle:${Versions.gradleVersion}"

    val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
    val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jre7:${Versions.kotlinVersion}"
    val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}"

    val appCompat = "com.android.support:appcompat-v7:${Versions.supportLibraryVersion}"
    val recyclerView = "com.android.support:recyclerview-v7:${Versions.supportLibraryVersion}"
    val design = "com.android.support:design:${Versions.supportLibraryVersion}"

    val koin = "org.koin:koin-android:${Versions.koinVersion}"

    val rxjava = "io.reactivex.rxjava2:rxjava:${Versions.rxJavaVersion}"
    val rxkotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxKotlinVersion}"
    val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroidVersion}"

    val livedata = "android.arch.lifecycle:livedata:${Versions.liveDataVersion}"
    val paging = "android.arch.paging:runtime:${Versions.pagingVersion}"
    val room = "android.arch.persistence.room:runtime:${Versions.roomVersion}"
    val roomCompiler = "android.arch.persistence.room:compiler:${Versions.roomVersion}"

    val stetho = "com.facebook.stetho:stetho:${Versions.stethoVersion}"

    val glide = "com.github.bumptech.glide:glide:${Versions.glideVersion}"
    val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glideVersion}"

    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofitVersion}"
    val retrofitMoshi = "com.squareup.retrofit2:converter-moshi:${Versions.retrofitVersion}"

    val moshi = "com.squareup.moshi:moshi:${Versions.moshiVersion}"
    val moshiKotlin = "com.squareup.moshi:moshi-kotlin:${Versions.moshiVersion}"

    val kotlinx = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinxVersion}"

    val timber = "com.jakewharton.timber:timber:${Versions.timberVersion}"

    val flexbox = "com.google.android:flexbox:${Versions.flexBoxVersion}"

    val androidKtx = "androidx.core:core-ktx:${Versions.androidKtxVersion}"

    val maps = "com.google.android.gms:play-services-maps:${Versions.playServicesVersion}"
    val mapUtils = "com.google.maps.android:android-maps-utils:${Versions.mapUtilVersion}"

    val constraintLayout = "com.android.support.constraint:constraint-layout:${Versions.constraintLayoutVersion}"
}