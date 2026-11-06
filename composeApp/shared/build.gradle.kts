@file:OptIn(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCacheApi::class)

import org.jetbrains.kotlin.gradle.plugin.mpp.DisableCacheInKotlinVersion

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.stability.analyzer)
}

kotlin {
    jvmToolchain(17)
    androidTarget {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
    iosArm64 {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-Xdisable-phases=RemoveRedundantCallsToStaticInitializersPhase")
        }
    }
    iosSimulatorArm64 {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
    // iosX64 is intentionally not enabled: several UI dependencies publish iOS ARM slices only.

    cocoapods {
        version = "1.0"
        summary = "Shared Kotlin code for the app"
        homepage = "https.example.com"
        ios.deploymentTarget = "16.0"
        // Point to the top-level iOS app Podfile
        podfile = project.file("../../iosApp/Podfile")

        pod("GoogleSignIn") {
            extraOpts += listOf("-compiler-option", "-fmodules")
        }



        // Pod name and framework name should match how Swift imports it: `import ComposeApp`
        name = "ComposeApp"
        framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
            disableNativeCache(
                version = DisableCacheInKotlinVersion.`2_4_0`,
                reason = "Temporary workaround for mpfilepicker Kotlin/Native cache failure on iOS simulator."
            )
        }
    }


//    listOf(
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach { iosTarget ->
//        iosTarget.binaries.framework {
//            baseName = "shared"
//            isStatic = true
//        }
//    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")
            implementation(libs.crashkios.crashlytics)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlin.stdlib)
            implementation(libs.datetime)
            implementation(libs.richeditor.compose)
            implementation(libs.mpfilepicker)


            implementation(libs.kotlinx.serialization.json.v180rc)

            //firebase functions
            implementation(libs.firebaseFirestore)
            implementation(libs.firebaseFunctions)
            implementation(libs.firebaseStorage)
            implementation(libs.firebaseAuth)
            implementation(libs.firebaseAnalytics)

            // koin
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

//            implementation(libs.kmpauth.google) //Google One Tap Sign-In
//            implementation(libs.kmpauth.firebase) //Integrated Authentications with Firebase
//            implementation(libs.kmpauth.uihelper) //UiHelper SignIn buttons (AppleSignIn, GoogleSignInButton)

            implementation(compose.materialIconsExtended)

            implementation("io.coil-kt.coil3:coil-compose:3.5.0")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.5.0")

            implementation(libs.in1.app.review.kmp.google.play)

            implementation(libs.purchases.core)
            implementation(libs.purchases.datetime)   // Optional
            implementation(libs.purchases.either)     // Optional
            implementation(libs.purchases.result)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.datastore)
            implementation(libs.datastorePreferences)
            implementation(libs.ktor.client.core)
            implementation(libs.vico.compose)
            implementation(libs.vico.compose.m3)

            implementation("co.touchlab:kermit:2.0.8")
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.accompanistPermissions)
            implementation(libs.coreKtx)
            implementation(libs.workRuntimeKtx)


            //koin
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation(libs.coreSplashscreen)


            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.playServicesAds)
            implementation(libs.googleid)
            implementation(libs.credentials)
            implementation(libs.credentialsPlayServicesAuth)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.crashlytics)
            implementation("com.google.firebase:firebase-messaging")
            implementation(libs.kmpnotifier.push.firebase)
        }

        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }

        named { it.lowercase().startsWith("ios") }.configureEach {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }
}


// Store versions come from release/releases/<version>.json via fastlane.
// Signing secrets stay in the environment, never Gradle command-line properties.
val storeVersionName = providers.environmentVariable("RELEASE_VERSION_NAME").orNull
val storeVersionCode = providers.environmentVariable("RELEASE_VERSION_CODE").orNull
require((storeVersionName == null) == (storeVersionCode == null)) {
    "Set both RELEASE_VERSION_NAME and RELEASE_VERSION_CODE for a store build."
}
storeVersionName?.let {
    require(Regex("[0-9]+\\.[0-9]+\\.[0-9]+").matches(it)) { "Invalid release version." }
}
storeVersionCode?.let {
    require((it.toLongOrNull() ?: 0L) in 1L..2100000000L) { "Invalid Android version code." }
}
val storeSigningNames = listOf(
    "ANDROID_KEYSTORE_PATH", "ANDROID_KEYSTORE_STORE_PASSWORD",
    "ANDROID_KEYSTORE_KEY_ALIAS", "ANDROID_KEYSTORE_KEY_PASSWORD",
)
val storeSigning = storeSigningNames.associateWith { providers.environmentVariable(it).orNull }
val hasStoreSigning = storeSigning.values.all { !it.isNullOrBlank() }
require(storeSigning.values.all { it.isNullOrBlank() } || hasStoreSigning) {
    "Incomplete Android store signing environment."
}
val requireStoreSigning = providers.environmentVariable("RELEASE_SIGNING_REQUIRED").orNull == "true"
require(!requireStoreSigning || (hasStoreSigning && storeVersionName != null)) {
    "Store builds require explicit release versions and all Android signing environment variables."
}

android {
    namespace = "org.ballistic.dreamjournalai.shared"
    compileSdk = 37

    sourceSets.getByName("main") {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/commonMain/resources", "src/androidMain/res")
        resources.srcDirs("src/commonMain/resources")
    }

    defaultConfig {
        applicationId = "org.ballistic.dreamjournalai"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = storeVersionCode?.toInt() ?: 106
        versionName = storeVersionName ?: "1.3.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
    signingConfigs {
        if (hasStoreSigning) {
            create("storeRelease") {
                storeFile = file(storeSigning.getValue("ANDROID_KEYSTORE_PATH")!!)
                storePassword = storeSigning.getValue("ANDROID_KEYSTORE_STORE_PASSWORD")
                keyAlias = storeSigning.getValue("ANDROID_KEYSTORE_KEY_ALIAS")
                keyPassword = storeSigning.getValue("ANDROID_KEYSTORE_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasStoreSigning) signingConfigs.getByName("storeRelease") else null
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
    buildFeatures.compose = true
}


// Use the final artifact provider, including existing bundle verification transforms.
// Never choose an older AAB by scanning build directories.
androidComponents.onVariants(androidComponents.selector().withBuildType("release")) { variant ->
    val finalBundle = variant.artifacts.get(com.android.build.api.artifact.SingleArtifact.BUNDLE)
    tasks.register("exportStoreReleaseBundle") {
        group = "release"
        description = "Export the final, explicitly signed bundle for the reviewed release."
        dependsOn("bundleRelease")
        inputs.file(finalBundle)
        val exportedBundle = rootProject.layout.buildDirectory.file(
            "release/${storeVersionName ?: "unconfigured"}/app.aab"
        )
        outputs.file(exportedBundle)
        doFirst {
            require(requireStoreSigning && hasStoreSigning && storeVersionName != null) {
                "Use the fastlane build_test lane with reviewed release notes and store signing."
            }
        }
        doLast {
            val destination = exportedBundle.get().asFile
            destination.parentFile.mkdirs()
            finalBundle.get().asFile.copyTo(destination, overwrite = true)
        }
    }
}
