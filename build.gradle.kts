plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "com.github.tejaslamba2006"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.74-stable")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.build {
    dependsOn(tasks.jar)
}

tasks.runServer {
    // Pulls the latest Paper build for this version automatically.
    minecraftVersion("26.1.2")
    downloadPlugins {
        hangar("ProtocolLib", "5.4.0")
    }
}
