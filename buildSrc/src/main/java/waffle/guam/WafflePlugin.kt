package waffle.guam

import org.gradle.api.Plugin
import org.gradle.api.Project
import waffle.guam.docker.DockerBuildTask
import org.gradle.kotlin.dsl.*

class WafflePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val task = target.tasks.create<DockerBuildTask>("dockerBuild")
        task.group = "docker"
    }
}