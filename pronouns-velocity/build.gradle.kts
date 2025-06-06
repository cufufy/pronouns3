plugins {
    java
    id("com.gradleup.shadow") version "8.3.6"
    id("net.kyori.blossom") version "1.3.1"
    id("pronouns.conventions")
    id("pronouns.publishable")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    shadow(project(":pronouns-common"))
    shadow(libs.cloud.velocity)
    shadow(libs.hikari) {
        exclude("org.slf4j")
    }
}

blossom {
    val file = "src/main/java/net/lucypoulton/pronouns/velocity/BuildConstants.java"
    replaceToken("\${version}", version, file)
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
}
