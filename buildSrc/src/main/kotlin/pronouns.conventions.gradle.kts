import org.gradle.api.JavaVersion

plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    processResources {
        expand("version" to project.version)
    }
}
