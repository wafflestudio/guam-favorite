plugins {
    id("io.github.wafflestudio.base")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("com.h2database:h2")
//    implementation("mysql:mysql-connector-java")
}
