plugins {
    java
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(platform("com.c12e.cortex.profiles:platform-dependencies"))
    api("com.c12e.cortex.profiles:profiles-sdk")
    implementation(project(":local-clients"))

    annotationProcessor("info.picocli:picocli-codegen:4.6.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

project.setProperty("mainClassName", "com.c12e.cortex.examples.bigquery.BigQuery")

tasks.distZip {
    enabled = false
}