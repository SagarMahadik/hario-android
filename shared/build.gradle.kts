plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)

    id("kotlin-kapt")
}

android {
    namespace = "com.example.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    //noinspection UseTomlInstead
    api("org.reduxkotlin:redux-kotlin-threadsafe-jvm:0.6.0")
    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.okhttp3:okhttp:4.9.1")
    api("com.squareup.retrofit2:converter-gson:2.9.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    api("androidx.security:security-crypto:1.1.0-alpha03")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(kotlin("reflect"))

    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
}