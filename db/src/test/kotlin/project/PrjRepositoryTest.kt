package waffle.guam.project

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest

@DatabaseTest(["projects/image.sql", "projects/project.sql", "projects/stack.sql", "projects/projectStack.sql", "task/image.sql", "task/user.sql", "task/task.sql", "task/task_message.sql"])
class PrjRepositoryTest @Autowired constructor(
    private val projectRepository: ProjectRepository
) {

    @Transactional
    @Test
    fun fetchNothing() {
        println("******************************")
        projectRepository.findAll().forEach {
            println(it)
        }
        println("*******************************")
    }
    /**
     *  Fetches Nothing
     */

    @Transactional
    @Test
    fun search() {
        println("******************************")
        projectRepository.findAll(
            ProjectSpec.search(due = "ONE", position = "FRONTEND")
        ).forEach {
            println(it.due)
        }
        println("******************************")
    }

    /**
     *  Fetches Only Image & Filter works fine
     */

    @Transactional
    @Test
    fun single() {
        println("******************************")
        projectRepository.findById(
            1
        ).let {
            println(it)
        }
        println("******************************")
    }
    /**
     *  should fetch Image at once
     */
}
