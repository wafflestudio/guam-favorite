plugins {
    id("io.github.wafflestudio.base")
}

dependencies {
    implementation(project(":db"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation(project(":test"))
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.1.RELEASE")
}

dependencies {
    implementation("com.google.firebase:firebase-admin:7.1.0")
}
