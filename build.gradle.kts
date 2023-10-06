import org.jreleaser.model.Active
import org.jreleaser.model.Distribution.DistributionType
import java.nio.file.Paths

plugins {
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("org.jreleaser") version "1.7.0"
    id("com.karmanno.plugins.semver") version "2.0.0"
    `java-library`
    `maven-publish`
    jacoco
}

group = "nl.ordina"

val httpkVersion = "4.41.3.0"
val kotestVersion = "5.6.1"
val mockkVersion = "1.13.5"

kotlin {
    jvmToolchain(17)
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
            url = uri("https://maven.pkg.github.com/Ordina-Group/github-kotlin-client")
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
        website.set("https://github.com/Ordina-Group/github-kotlin-client")
        authors.add("Donovan de Kuiper")
        license.set("APACHE-2.0")
        copyright.set("Copyright © 2023 Ordina")
    }

    release {
        github {
            overwrite.set(true)

            changelog {
                formatted.set(Active.ALWAYS)
                preset.set("conventional-commits")
                contributors {
                    format.set(
                        "- {{contributorName}}{{#contributorUsernameAsLink}} ({{.}}){{/contributorUsernameAsLink}}"
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
    implementation("org.http4k:http4k-core:$httpkVersion")
    implementation("org.http4k:http4k-format-kotlinx-serialization:$httpkVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

repositories {
    mavenCentral()
}