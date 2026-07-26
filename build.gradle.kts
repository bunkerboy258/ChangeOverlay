import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("io.github.java-diff-utils:java-diff-utils:4.15")

    intellijPlatform {
        local(providers.gradleProperty("platformPath"))
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
    }

    testImplementation(kotlin("test-junit"))
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = "261"
            untilBuild = "261.*"
        }

        vendor {
            name = "BBBWork"
        }
    }

    pluginVerification {
        ides {
            local(providers.gradleProperty("platformPath"))
        }
    }
}

tasks {
    wrapper {
        gradleVersion = "9.0.0"
        distributionType = Wrapper.DistributionType.BIN
    }
}
