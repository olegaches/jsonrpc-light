plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    `maven-publish`
    `java-library`
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = "17"

        // For creation of default methods in interfaces
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
}
publishing {
    publications {
        create<MavenPublication>("jsonrpc-light") {
            from(components["java"])

            groupId = "com.olegaches"
            artifactId = "jsonrpc-light"
            version = "1.0.0"
        }
    }
}