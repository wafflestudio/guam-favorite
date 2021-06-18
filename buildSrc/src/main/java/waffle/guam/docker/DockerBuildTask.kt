package waffle.guam.docker

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByName
import org.gradle.api.tasks.bundling.Jar
import java.io.File

open class DockerBuildTask : DefaultTask() {
    @TaskAction
    fun buildDockerWithTemplate() {
        val dir = project.mkdir(File(project.buildDir, "tmp")).also { dir ->
            File(dir, "Dockerfile").also { docker ->
                docker.writeText(
                    DockerBuildTask::class.java.classLoader
                        .getResourceAsStream("template")!!
                        .use { it.reader().readText() }
                )
            }
        }

        project.copy {
            val jarTask = project.tasks.getByName<Jar>("bootJar")

            from(jarTask.archiveFile) {
                rename { "waffle.jar" }
            }

            into(dir)
        }

        project.exec {
            workingDir(dir)
            commandLine("docker", "build", "-t", "gcr.io/waffle-guam/projects", ".")
        }
    }
}