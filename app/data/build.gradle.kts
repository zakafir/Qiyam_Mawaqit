plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0" // match your Kotlin version
}
android {
    namespace = "com.zakafir.data"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":app:domain"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
    implementation("io.ktor:ktor-client-cio:2.3.11")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")

    // Ktor Android engine and logging
    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation("io.ktor:ktor-client-logging:2.3.11")

    // kotlinx serialization JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation(libs.bundles.koin)
}
