
plugins {
    id("org.springframework.boot") version "2.3.4.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm") version "1.4.20"
    kotlin("plugin.spring") version "1.4.20"
    kotlin("plugin.jpa") version "1.4.20" apply false
   // kotlin("kapt") version "1.4.20"
}

group = "waffle.guam"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        jcenter()
    }
}

subprojects{
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    apply {
        plugin("kotlin")
        plugin("kotlin-spring")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin( "kotlin-allopen")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation("io.springfox:springfox-swagger2:2.9.2")
        implementation("io.springfox:springfox-swagger-ui:2.9.2")
        implementation("com.squareup.okhttp3:okhttp:3.12.0")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
    }
}


