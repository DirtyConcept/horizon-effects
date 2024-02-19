plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.1")
}

group = "net.horizonmines"
version = "1.0.0-b2"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
