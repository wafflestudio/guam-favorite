plugins {
    id("io.github.wafflestudio.base") version "1.1.3"
    id("io.github.wafflestudio.springboot") version "1.1.3" apply false
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
        implementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
        testImplementation("io.kotest:kotest-property:$kotestVersion")
        testImplementation("io.kotest:kotest-extensions-spring:$kotestVersion")
        testImplementation("io.mockk:mockk:$mockkVersion")
    }
}
