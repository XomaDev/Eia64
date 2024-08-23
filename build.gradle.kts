import org.teavm.gradle.api.OptimizationLevel

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

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    implementation(teavm.libs.jsoApis)
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    manifest {
        attributes("Main-Class" to "space.themelon.eia64.Main")
    }
    from({
        configurations.compileClasspath.get().filter {
            it.exists()
        }.map {
            if (it.isDirectory) it else project.zipTree(it)
        }
    })
    with(tasks.jar.get())
    duplicatesStrategy = DuplicatesStrategy.WARN
}

kotlin {
    jvmToolchain(11)
}

teavm {
    js {
        sourceMap = true
        obfuscated = false
        addedToWebApp = true
        optimization.value(OptimizationLevel.NONE)
    }
    all {
        mainClass = "space.themelon.eia64.tea.TeaMain"
    }
}

tasks.register<Copy>("unzipWar") {
    group = "build"
    val buildDirectory = layout.buildDirectory.asFile.get().absolutePath
    from(zipTree("$buildDirectory/libs/${project.name}-${project.version}.war"))
    into("$buildDirectory/pub")
    dependsOn("build")
}

tasks.named("build") {
    finalizedBy("unzipWar")
}