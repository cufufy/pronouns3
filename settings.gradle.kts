rootProject.name = "pronouns"

include("pronouns-core", "pronouns-common", "pronouns-paper", "pronouns-velocity")

pluginManagement {
    repositories {
        maven( "https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://libraries.minecraft.net")
    }
    versionCatalogs.create("libs") {
        version("adventure", "4.17.0")
        version("cloud", "1.8.4")
        listOf("api", "text-minimessage").forEach {
            library("adventure.$it", "net.kyori", "adventure-$it").versionRef("adventure")
        }
        listOf("core", "fabric", "paper", "velocity").forEach {
            library("cloud.$it", "cloud.commandframework", "cloud-$it").versionRef("cloud")
        }
        library("hikari", "com.zaxxer:HikariCP:5.1.0")

        library("gson", "com.google.code.gson:gson:2.10.1")

        library("slf4j", "org.slf4j:slf4j-api:2.0.6")
    }
}
