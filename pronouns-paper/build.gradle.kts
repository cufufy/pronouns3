plugins {
    java
    id("com.gradleup.shadow") version "8.3.6"
    id("pronouns.conventions")
    id("pronouns.publishable")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi")
}

val minecraftVersion = "1.21.5"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    shadow(project(":pronouns-common"))
    shadow(libs.cloud.paper)
    shadow(libs.hikari) {
        exclude("org.slf4j")
    }
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly(libs.gson)
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier.set("")
        relocate("cloud.commandframework", "net.lucypoulton.pronouns.shadow.cloud")
        relocate("io.leangen.geantyref", "net.lucypoulton.pronouns.shadow.geantyref")
        relocate("com.zaxxer.hikari", "net.lucypoulton.pronouns.shadow.hikari")

        minimize {
            exclude(project(":pronouns-core"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
    processResources {
        expand("version" to project.version)
    }
}

modrinth {
    gameVersions.add(minecraftVersion)
    loaders.add("paper")
    versionName.set("$version for Paper")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}
