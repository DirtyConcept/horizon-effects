import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;

plugins {
    id("java-conventions")

    id("io.papermc.paperweight.userdev") version "1.5.6"
    id("xyz.jpenilla.run-paper") version "2.1.0"

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {

    implementation(project(":effects-api"))
    annotationProcessor("io.micronaut:micronaut-inject-java:4.1.9");
    implementation("io.micronaut:micronaut-inject-java:4.1.9")

    implementation("dev.triumphteam:triumph-gui:3.1.5")
    implementation("com.github.SadGhostYT:Espresso:29fa1f0dbc")
    
    implementation("org.mongodb:mongo-java-driver:3.12.12")

    annotationProcessor("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")
    compileOnly("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")

    paperweight.paperDevBundle("1.19.1-R0.1-SNAPSHOT")
}

tasks.withType<ShadowJar> {
    relocate("dev.triumphteam.gui", "net.horizonmines.effects.gui")
}

description = "effects-plugin"
