plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.flowday.core.network"
    compileSdk = 36


    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


}

dependencies {
// Domain
    implementation(project(":domain"))

    // Retrofit
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)

    // OkHttp
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    //explanation of this dependency here "https://youtrack.jetbrains.com/issue/KTIJ-31549"
    implementation(libs.androidx.annotation.experimental)
}