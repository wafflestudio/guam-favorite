plugins {
    id("io.github.wafflestudio.base") version "1.0.1"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

group = "waffle.guam"
version = "1.0-SNAPSHOT"

subprojects {
    apply(plugin = "io.github.wafflestudio.base")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        maven { url = uri("https://repo1.maven.org/maven2/") }
        mavenCentral()
    }

    val kotestVersion = "4.4.+"
    val mockkVersion = "1.10.+"

    dependencies {
        testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
        testImplementation("io.kotest:kotest-property:$kotestVersion")
        testImplementation("io.kotest:kotest-extensions-spring:$kotestVersion")
        testImplementation("io.mockk:mockk:$mockkVersion")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
}
