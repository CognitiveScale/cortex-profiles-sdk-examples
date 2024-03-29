plugins {
    java
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    // project dependencies
    api(platform("com.c12e.cortex.profiles:platform-dependencies"))
    api("com.c12e.cortex.profiles:profiles-sdk")
    implementation(project(":local-clients"))

    // CLI framework
    annotationProcessor("info.picocli:picocli-codegen:4.6.3")

    // test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit-pioneer:junit-pioneer:1.7.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

project.setProperty("mainClassName", "com.c12e.cortex.examples.joinconn.JoinConnections")

tasks.distZip {
    enabled = false
}