plugins {
    id("io.github.wafflestudio.base")
}

dependencies {
    implementation(project(":db"))
    testImplementation(project(":test"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.3")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.1.RELEASE")
}

dependencies {
    implementation("com.google.firebase:firebase-admin:7.1.0")
}
