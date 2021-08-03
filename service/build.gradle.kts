plugins {
    id("io.github.wafflestudio.base")
}

dependencies {
    testImplementation(project(":test"))
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("com.h2database:h2")
//    implementation("mysql:mysql-connector-java")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.1.RELEASE")
}

dependencies {
    implementation("com.google.firebase:firebase-admin:7.1.0")
}
