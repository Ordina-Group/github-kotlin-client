import org.jreleaser.model.Active
import org.jreleaser.model.Distribution.DistributionType
import java.nio.file.Paths

plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("org.jreleaser") version "1.7.0"
    id("pl.allegro.tech.build.axion-release") version "1.18.13"
    id("org.jetbrains.dokka") version "2.0.0"
    `java-library`
    `maven-publish`
    jacoco
}

group = "com.soprasteria"
version = scmVersion.version

val http4kVersion = "6.15.1.0"
val kotestVersion = "5.9.1"
val mockkVersion = "1.14.3"
val coroutinesVersion = "1.9.0"

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/soprasteria/github-kotlin-client")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

jreleaser {
    project {
        description.set("A library for using the GitHub API from the JVM")
        links.homepage.set("https://github.com/soprasteria/github-kotlin-client")
        authors.add("Donovan de Kuiper")
        license.set("APACHE-2.0")
        copyright.set("Copyright © 2026 Sopra Steria")
    }

    release {
        github {
            overwrite.set(true)

            changelog {
                formatted.set(Active.ALWAYS)
                preset.set("conventional-commits")
                contributors {
                    format.set(
                        "- {{contributorName}}{{#contributorUsernameAsLink}} ({{.}}){{/contributorUsernameAsLink}}",
                    )
                }
            }

            prerelease {
                pattern.set(".*-RC\\d*")
            }
        }

        distributions {
            create("github-kotlin-client") {
                distributionType.set(DistributionType.SINGLE_JAR)
                artifact {
                    path.set(Paths.get("build/libs/${project.name.get()}-${project.version.get()}.jar").toFile())
                }
            }
        }
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

dependencies {
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-client-apache:$http4kVersion")
    implementation("org.http4k:http4k-format-kotlinx-serialization:$http4kVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

repositories {
    mavenCentral()
}