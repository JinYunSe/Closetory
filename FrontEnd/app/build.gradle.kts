plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // ROOM
    id("com.google.devtools.ksp")

    // Ktlint
    id("org.jlleitschuh.gradle.ktlint")

    id("kotlin-kapt")
}

android {
    namespace = "com.ssafy.closetory"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ssafy.closetory"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
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
        mlModelBinding = true
    }

    ktlint {
        android.set(true)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit + Gson Converter
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Room (KSP)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // OkHttp (HTTP 클라이언트 엔진)
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // OkHttp Logging Interceptor (요청/응답 로그 출력용)
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Material (Chip 포함)
    implementation("com.google.android.material:material:1.12.0")

    // FlexboxLayoutManager (줄바꿈 태그 배치)
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Image Segmenter / Interactive Image Segmenter (이미지 배경 제거를 위해 추가)
    implementation(libs.mediapipe.tasks.vision)
}
