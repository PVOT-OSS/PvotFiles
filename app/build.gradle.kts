// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

import java.util.Properties

plugins {
    alias(libs.plugins.pvot.android.application)
    alias(libs.plugins.pvot.android.compose)
    alias(libs.plugins.pvot.hilt)
}

// From keystore.properties at the project root (not in git) or the FILES_KEYSTORE_* environment
// variables a CI job sets; without either the release build is signed with the debug key.
val releaseSigning: Map<String, String>? = run {
    val file = rootProject.file("keystore.properties")
    val props = Properties().apply { if (file.exists()) file.inputStream().use(::load) }
    fun value(key: String, env: String) = props.getProperty(key) ?: System.getenv(env)
    val storeFile = value("storeFile", "FILES_KEYSTORE_FILE") ?: return@run null
    mapOf(
        "storeFile" to storeFile,
        "storePassword" to value("storePassword", "FILES_KEYSTORE_PASSWORD").orEmpty(),
        "keyAlias" to value("keyAlias", "FILES_KEY_ALIAS").orEmpty(),
        "keyPassword" to value("keyPassword", "FILES_KEY_PASSWORD").orEmpty(),
    )
}

android {
    namespace = "org.prauga.files"

    signingConfigs {
        releaseSigning?.let { key ->
            create("release") {
                storeFile = rootProject.file(key.getValue("storeFile"))
                storePassword = key.getValue("storePassword")
                keyAlias = key.getValue("keyAlias")
                keyPassword = key.getValue("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "org.prauga.files"
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String
    }

    buildTypes {
        debug { applicationIdSuffix = ".dev" }
        release {
            optimization { enable = true }
            signingConfig =
                signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui.compose)
    implementation(project(":core:data"))
    implementation(libs.pvot.design.system)
    implementation(libs.pvot.core.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.kotlinx.coroutines.test)
}
