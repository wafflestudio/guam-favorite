publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${project.rootProject.group}"
            artifactId = project.name
            version = "${project.rootProject.version}"

            from(components["java"])
        }
    }
}
