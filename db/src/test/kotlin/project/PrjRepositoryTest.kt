package waffle.guam.project

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest

@DatabaseTest(["images/image.sql", "projects/project.sql", "projects/stack.sql", "projects/projectStack.sql", "task/user.sql", "task/task.sql", "task/task_message.sql"])
class PrjRepositoryTest @Autowired constructor(
    private val projectRepository: ProjectRepository
) {

    @Transactional
    @Test
    fun fetchNothing() {
        println("******************************")
        projectRepository.findAll().forEach {
            println(it.id)
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
        projectRepository.search(
            due = "ONE", position = "FRONTEND"
        ).forEach {
            println(it.id)
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
        ).orElseThrow(::Exception).let {
            println(it.id)
        }
        println("******************************")
    }
    /**
     *  should fetch Image at once
     */

    @Transactional
    @Test
    fun imminent() {
        println("******************************")
        projectRepository.imminent().map {
            println(it.frontHeadcount)
            println(it.backHeadcount)
            println(it.designerHeadcount)
        }
        println("******************************")
    }
    /**
     *  제발 돼라
     */
}
