plugins {
    id("org.springframework.boot")
    id("waffle.guam.waffle-plugin")
}

dependencies {
    implementation(project(":service"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

