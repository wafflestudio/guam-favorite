package waffle.guam.stack

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest

@DatabaseTest(["projects/image.sql", "projects/project.sql", "projects/stack.sql", "projects/projectStack.sql", "task/image.sql", "task/user.sql", "task/task.sql", "task/task_message.sql"])
class StackRepositoryTest @Autowired constructor(
    private val stackRepository: StackRepository
){

    @Transactional
    @Test
    fun fetchSingle() {
        stackRepository.findById(1).map {
            println(it)
        }
    }

    @Transactional
    @Test
    fun fetchDefault() {
        stackRepository.findAllByIds(listOf(1, 2, 3)).map {
            println(it)
        }
    }
}