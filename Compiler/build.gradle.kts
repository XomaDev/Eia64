plugins {
    id("java")
    kotlin("jvm")
    id("war")
    id("org.teavm") version "0.9.2"
}

group = "space.themelon.eia64"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
    implementation(teavm.libs.jsoApis)
}

teavm {
    js {
        addedToWebApp = true
        mainClass.set("space.themelon.eia64.compiler.web.WebMain")
        targetFileName.set("example.js")
    }
}

tasks.register<Copy>("unzipWar") {
    group = "build"

    // just unzip .war after build is finishedc
    val buildDirectory = layout.buildDirectory.asFile.get().absolutePath
    from(zipTree("$buildDirectory/libs/${project.name}-${project.version}.war"))
    into("$buildDirectory/unzipped-war")
    dependsOn("build")
}

tasks.named("build") {
    finalizedBy("unzipWar")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}