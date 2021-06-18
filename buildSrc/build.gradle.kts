plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    testImplementation("junit:junit:4.13")
}

gradlePlugin {
    plugins {
        create("waffle-plugin") {
            id = "waffle.guam.waffle-plugin"
            implementationClass = "waffle.guam.WafflePlugin"
        }
    }
}
