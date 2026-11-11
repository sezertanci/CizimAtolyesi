plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
}

// Yükleme anahtarı bilgileri repoda tutulmaz; ~/.gradle/gradle.properties veya
// ORG_GRADLE_PROJECT_<ad> ortam değişkenlerinden okunur. Yoksa sürüm paketi imzasız üretilir.
fun uploadProperty(name: String): String? = providers.gradleProperty("drawingWorkshopUpload$name").orNull
val uploadStoreFile = uploadProperty("StoreFile")

android {
    namespace = "dev.sezertanci.drawingworkshop"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.sezertanci.drawingworkshop"
        minSdk = 29
        targetSdk = 36
        versionCode = 5
        versionName = "1.3.1"
    }

    signingConfigs {
        if (uploadStoreFile != null) {
            create("upload") {
                storeFile = file(uploadStoreFile)
                storePassword = uploadProperty("StorePassword")
                keyAlias = uploadProperty("KeyAlias")
                keyPassword = uploadProperty("KeyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("upload")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
}
