plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
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
        }
    }
    iosSimulatorArm64 {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
    // (optional if you test on Intel Mac)
    // iosX64()

    cocoapods {
        version = "1.0"
        summary = "Shared Kotlin code for the app"
        homepage = "https.example.com"
        ios.deploymentTarget = "13.0"
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


            //Image
            implementation(libs.landscapist.coil3)

            implementation(libs.kotlinx.serialization.json.v180rc)

            //firebase functions
            implementation(libs.firebaseFirestore)
            implementation(libs.firebaseFunctions)
            implementation(libs.firebaseStorage)
            implementation(libs.firebaseAuth)

            // koin
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

//            implementation(libs.kmpauth.google) //Google One Tap Sign-In
//            implementation(libs.kmpauth.firebase) //Integrated Authentications with Firebase
//            implementation(libs.kmpauth.uihelper) //UiHelper SignIn buttons (AppleSignIn, GoogleSignInButton)

            //openai
            implementation(project.dependencies.platform(libs.openaiClientBom))
            implementation(libs.openaiClient)

            implementation(compose.materialIconsExtended)

            implementation("io.coil-kt.coil3:coil-compose:3.3.0")

            implementation(libs.in1.app.review.kmp.google.play)

            implementation(libs.purchases.core)
            implementation(libs.purchases.datetime)   // Optional
            implementation(libs.purchases.either)     // Optional
            implementation(libs.purchases.result)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.datastore)
            implementation(libs.datastorePreferences)
            implementation(libs.ktor.client.core)
            implementation(libs.vico.multiplatform)
            implementation(libs.vico.multiplatform.m3)

            implementation("co.touchlab:kermit:2.0.8")
            implementation("network.chaintech:cmpcharts:2.0.6")
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


            //koin
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation(libs.coreSplashscreen)


            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")


            implementation(libs.playServicesAds)
            implementation(libs.googleid)
            implementation(libs.credentials)
            implementation(libs.credentialsPlayServicesAuth)
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:33.8.0"))
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


android {
    namespace = "org.ballistic.dreamjournalai.shared"
    compileSdk = 36

    sourceSets.getByName("main") {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/commonMain/resources", "src/androidMain/res")
        resources.srcDirs("src/commonMain/resources")
    }

    defaultConfig {
        applicationId = "org.ballistic.dreamjournalai"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 80
        versionName = "1.3.4"
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
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Adjust the signing config as necessary
            signingConfig = signingConfigs.getByName("debug")
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
