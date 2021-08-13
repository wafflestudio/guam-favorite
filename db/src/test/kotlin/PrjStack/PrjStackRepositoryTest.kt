package waffle.guam.PrjStack

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest
import waffle.guam.projectstack.ProjectStackRepository

@DatabaseTest(["projects/image.sql", "projects/project.sql", "projects/stack.sql", "projects/projectStack.sql", "task/image.sql", "task/user.sql", "task/task.sql", "task/task_message.sql"])
class PrjStackRepositoryTest @Autowired constructor(
    private val projectStackRepository: ProjectStackRepository
) {

    @Transactional
    @Test
    fun fetchNothing() {
        projectStackRepository.findAll().forEach {
            println("******************************")
            println(it)
            println("*******************************")
        }
    }

    @Transactional
    @Test
    fun findById() {
        projectStackRepository.findByProjectId(1).let {
            println("******************************")
            println(it)
            println("*******************************")
        }
    }

    @Transactional
    @Test
    fun findAllById() {
        projectStackRepository.findAllByProjectIds(listOf(1, 2)).forEach {
            println("******************************")
            println(it)
            println("*******************************")
        }
    }
}
