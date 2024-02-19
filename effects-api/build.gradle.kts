plugins {
    id("java-conventions")
}

dependencies {
    compileOnly("io.micronaut:micronaut-inject-java:4.1.9")

    compileOnly("org.mongodb:mongo-java-driver:3.12.12")
}

description = "effects-api"