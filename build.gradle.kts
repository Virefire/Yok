import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    kotlin("jvm") version "1.6.21"
    `maven-publish`
}

group = "dev.virefire.yok"
version = "1.0.4"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.rikonardo.com/releases")
    }
}

dependencies {
    implementation("dev.virefire.kson:KSON:1.3.1")
    implementation("org.apache.httpcomponents:httpmime:4.5.13")
    implementation("io.ktor:ktor-client-cio:2.0.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.register("createProperties") {
    doLast {
        val f = File("$buildDir/resources/main/dev/virefire/yok/version.properties")
        File(f.parent).mkdirs()
        val p = Properties()
        p["version"] = project.version.toString()
        p.store(f.outputStream(), null)
    }
}

tasks.getByName("jar") {
    dependsOn("createProperties")
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            pom {
                name.set("Yok")
                description.set("Kotlin HTTP request library")
            }
        }
    }
    repositories {
        maven {
            val properties = Properties()
            properties.load(rootProject.file("publish.properties").inputStream())
            url = uri(properties["deployRepoUrl"].toString())
            credentials {
                username = properties["deployRepoUsername"].toString()
                password = properties["deployRepoPassword"].toString()
            }
        }
    }
}