import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("kotlin-android")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.3")
    buildFeatures.dataBinding = true

    defaultConfig {
        applicationId = "online.vapcom.dashboard"
        minSdkVersion(23)
        targetSdkVersion(29)    // пока нет исходников API 30 пользуемся API 29
        versionCode = 16

        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        setProperty("archivesBaseName", "dashboard-$versionName.$versionCode")
    }

    // конфиг подписи релизного APK-файла, параметры читаются из keystore.properties
    signingConfigs {
        create("release") {

            // keystorePropertiesFilename задан в gradle.properties или в командной строке, см. пример ниже
            val keystorePropertiesFile = rootProject.file(properties["keystorePropertiesFilename"] ?: "ks properties filename not found")
            val keystoreProperties = Properties()
            if (keystorePropertiesFile.exists()) {
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
            } else {
                println ("Cannot find keystore properties file.\n" +
                        "A) Create this file, B) use a different file, or C) use assembleDebug.\n" +
                        "A) Create ${keystorePropertiesFile.absolutePath}\n" +
                        "B) ./gradlew -PkeystorePropertiesFilename=release-keystore.properties assembleRelease\n" +
                        "C) ./gradlew assembleDebug\n")
            }

            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            isV1SigningEnabled = true
            isV2SigningEnabled = true

        }
    }

    buildTypes {
        getByName("release") {
            //NOTE: т.к. это тестовое задание, то сделаю здесь ремарку, что этот якобы релизный signingConfig - это honeypot,
            //      который нужен для отвлечения внимания мамкиных хакеров, укравших исходники.
            //      Настоящая релизная сборка подписывается сертификатом, который лежит в JKS-файле на секретной флэшке.
            //      Местный dashboard.keystore.jks используется только для подписи debug-сборок и на Google Play никогда не попадёт.

            //signingConfig = signingConfigs.getByName("release")

            isMinifyEnabled = true
            proguardFiles ("proguard-rules.pro", getDefaultProguardFile("proguard-android.txt"))
            // versionNameSuffix = "-release"
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")

            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


}

// Включает целевую платформу 1.8, иначе будет ошибка
// Cannot inline bytecode built with jvm target 1.8 into bytecode that is being built with jvm target 1.6
// см. https://coil-kt.github.io/coil/getting_started/#java-8
// см. https://stackoverflow.com/questions/48988778/cannot-inline-bytecode-built-with-jvm-target-1-8-into-bytecode-that-is-being-bui
tasks.withType < org.jetbrains.kotlin.gradle.tasks.KotlinCompile > {
    kotlinOptions.jvmTarget = "1.8"
}


dependencies {

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.3")

    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    implementation("com.google.android.material:material:1.2.0")


    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test:core:1.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    // тестов UI пока нет
    // androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")

    // Testing code should not be included in the main code.
    // Once https://issuetracker.google.com/128612536 is fixed this can be fixed.
    // если определять в androidTestImplementation, то получаем исключение Unable to resolve activity for: Intent
    //debugImplementation("androidx.fragment:fragment-testing:1.2.5")

}
