import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.ComposeHotRun
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.hotReload)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.spotless)
}

kotlin {
    androidTarget {
        //https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.logging)
            implementation(libs.voyager.navigator)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.voyager.transitions)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.compose)
            implementation(libs.mvvm.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.okhttp)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.libvlc.android)
            implementation(libs.koin.android)
            implementation("org.mozilla.geckoview:geckoview:130.0.20240913135723")
            implementation("com.google.android.gms:play-services-location:21.0.1")
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.vlcj)

        }

    }
}


// проставление кооперайта в kt файлы
// запуск ./gradlew :composeApp:spotlessApply
spotless {
    // Kotlin: ТОЛЬКО шапка, никаких проверок
    kotlin {
        target("**/*.kt")
        licenseHeader(
            """
            /*
             * Copyright (c) LLC "Centr Distribyucii"
             * All rights reserved.
             */
            """.trimIndent(),
            // вставлять перед первым package|import
            "(package|import)"
        )
    }

    // Java: ТОЛЬКО шапка
    java {
        target("**/*.java")
        licenseHeader(
            """
            /*
             * Copyright (c) LLC "Centr Distribyucii"
             * All rights reserved.
             */
            """.trimIndent()
        )
    }

    // XML: ТОЛЬКО шапка для layout/values/xml (исключаем mipmap/drawable/navigation/color/anim, т.к. там часто нет подходящего места)
    format("xml-license") {
        target(
            "**/src/**/res/layout/**/*.xml",
            "**/src/**/res/values/**/*.xml",
            "**/src/**/res/xml/**/*.xml"
        )
        targetExclude(
            "**/src/**/res/mipmap*/**",
            "**/src/**/res/drawable*/**",
            "**/src/**/res/navigation/**",
            "**/src/**/res/color/**",
            "**/src/**/res/anim*/**"
        )
        licenseHeader(
            """
            <!--
              Copyright (c) LLC "Centr Distribyucii"
              All rights reserved.
            -->
            """.trimIndent(),
            // вставить коммент прямо перед первым тегом (с учётом возможного <?xml ...?>)
            "(?s)\\A\\s*(?:<\\?xml.*?\\?>\\s*)?(?=<)"
        )
    }

    // Прочее
    format("misc") {
        target("**/*.gradle.kts", "**/*.md", "**/.gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}


android {
    namespace = "videoTrade.screonPlayer.app"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        targetSdk = 35
        resValue("string", "app_name", "Screon Player")


        applicationId = "videoTrade.screonPlayer.app.androidApp"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true           // обфускация + сжатие кода
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-android.pro"
            )

            resValue("string", "app_name", "ScreonPlayer") // имя приложения
        }
        debug {
            // Чтобы на dev-устройствах отличалось
            resValue("string", "app_name", "ScreonPlayer (debug)")
        }
    }
}

//https://developer.android.com/develop/ui/compose/testing#setup
dependencies {
    androidTestImplementation(libs.androidx.uitest.junit4)
    debugImplementation(libs.androidx.uitest.testManifest)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "screon"
            packageVersion = "1.0.0"
            vendor         = "Videotrade"

            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
                shortcut = true
                menuGroup = "screon"
                includeAllModules = true
                packageName = "screon"
            }

            modules("java.management")
        }

    }
}



//afterEvaluate {
//    val vlcSource = File(rootProject.projectDir, "composeApp/src/jvmMain/vlc")
//    val appDirProvider = layout.buildDirectory.dir("compose/binaries/main/app")
//
//    tasks.named("createDistributable") {
//        doLast {
//            val appDir = appDirProvider.get().asFile
//            val targetDir = File(appDir, "vlc")
//
//            if (vlcSource.exists()) {
//                vlcSource.copyRecursively(targetDir, overwrite = true)
//                println("✅ VLC скопирован в distributable: ${targetDir.absolutePath}")
//            } else {
//                println("⚠️ VLC не найден: ${vlcSource.absolutePath}")
//            }
//        }
//    }
//}



//https://github.com/JetBrains/compose-hot-reload
composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}
tasks.withType<ComposeHotRun>().configureEach {
    mainClass.set("MainKt")
}

tasks.register<Copy>("copyVlc") {
    from("$projectDir/src/jvmMain/resources/vlc")
    into("$buildDir/compose/binaries/main/app/screon/vlc")
    includeEmptyDirs = true
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    doFirst {
        println("📦 Копируем всю папку VLC в build/compose/binaries/main/app/screon/vlc")
    }
}
tasks.matching { it.name == "createDistributable" }.configureEach {
    finalizedBy("copyVlc")
}

tasks.withType<JavaExec> {
    systemProperty("java.library.path", file("src/jvmMain/resources/vlc").absolutePath)
}
