import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("org.jlleitschuh.gradle.ktlint")
    id("kotlin-kapt")
}

// local.properties 로드 (루트의 local.properties)
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

// 값이 없으면 Sync 자체가 죽어서 BuildConfig가 안 만들어질 수 있으니 기본값(fallback) 허용
fun prop(key: String, default: String): String =
    (localProps.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }) ?: default

val safeUrl = prop("CLOSETORY_BASE_URL", "http://10.0.2.2:8080/api/v1/")
    .trim().removeSuffix("/") + "/"

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

        // URL
        buildConfigField("String", "BASE_URL", "\"$safeUrl\"")

        // 나머지 상수들
        buildConfigField("String", "X_ACCESS_TOKEN", "\"${prop("X_ACCESS_TOKEN", "Authorization")}\"")
        buildConfigField("String", "X_REFRESH_TOKEN", "\"${prop("X_REFRESH_TOKEN", "X-REFRESH-TOKEN")}\"")
        buildConfigField("String", "USERID", "\"${prop("USERID", "userId")}\"")
        buildConfigField(
            "String",
            "SHARED_PREFERENCES_NAME",
            "\"${prop("SHARED_PREFERENCES_NAME", "SSAFY_CLOSETORY")}\""
        )
        buildConfigField("String", "COOKIES_KEY_NAME", "\"${prop("COOKIES_KEY_NAME", "cookies")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // 필요 시 디버그 옵션 추가
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        mlModelBinding = true
    }

    // tflite 모델 압축 방지
    androidResources {
        noCompress += "tflite"
    }

    ktlint { android.set(true) }
}

dependencies {
    // 비디오 의존성
    implementation("com.airbnb.android:lottie:6.1.0")

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
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment)
    implementation(libs.engage.core)
    implementation(libs.image.labeling.default.common)

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

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Material / Flexbox / Glide
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // 사진 리사이즈/압축
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // On-device image labeling (ML Kit)
    implementation("com.google.mlkit:image-labeling:17.0.8")

    // PhotoView : 핀치 줌(확대/축소) + 드래그 이동 가능 위젯
    implementation("io.getstream:photoview:1.0.3")
}
