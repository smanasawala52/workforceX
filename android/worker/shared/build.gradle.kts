import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    // iOS targets are disabled to allow the Android app to build on Windows.
    // To build for iOS, you must use a macOS machine.
    // val xcf = XCFramework()
    // listOf(
    //     iosX64(),
    //     iosArm64(),
    //     iosSimulatorArm64()
    // ).forEach {
    //     it.binaries.framework {
    //         baseName = "shared"
    //         xcf.add(this)
    //     }
    // }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Use 'api' to expose these dependencies to the 'app' module
                api("com.squareup.retrofit2:retrofit:2.9.0")
                api("com.squareup.retrofit2:converter-gson:2.9.0")
                api("com.squareup.okhttp3:logging-interceptor:4.12.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "com.workforcex.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
