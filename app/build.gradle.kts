
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.xiaohe.english"
    compileSdk = 36

    defaultConfig {
        // OSS 凭证经 CI Secret（OSS_AK_ID/OSS_AK_SECRET）注入；与原版 APK 内嵌值一致
        buildConfigField("String", "OSS_AK_ID", "\"${System.getenv("OSS_AK_ID") ?: project.findProperty("OSS_AK_ID") ?: ""}\"")
        buildConfigField("String", "OSS_AK_SECRET", "\"${System.getenv("OSS_AK_SECRET") ?: project.findProperty("OSS_AK_SECRET") ?: ""}\"")
        applicationId = "com.shangheedu.xiaoheapp.md3"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "2.2.3-md3.1"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystore/xiaohe.keystore")
            storePassword = System.getenv("KS_PASS") ?: "xiaohe-md3-2026"
            keyAlias = System.getenv("KS_ALIAS") ?: "xiaohe"
            keyPassword = System.getenv("KEY_PASS") ?: "xiaohe-md3-2026"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.tencent.mm.opensdk:wechat-sdk-android:6.8.30")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
