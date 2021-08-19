package waffle.guam.comment

import org.springframework.beans.factory.annotation.Autowired
import waffle.guam.annotation.DatabaseTest
import waffle.guam.task.TaskService
import waffle.guam.thread.ThreadRepository
import waffle.guam.user.UserRepository

@DatabaseTest(["comment/image.sql", "comment/user.sql", "comment/project.sql", "comment/task.sql", "comment/thread.sql", "comment/comment.sql"])
class CommentServiceCommandTest @Autowired constructor(
    private val commentRepository: CommentRepository,
    private val threadRepository: ThreadRepository,
    private val taskService: TaskService,
    private val userRepository: UserRepository
) {
    private val commentService = CommentServiceImpl(
        commentRepository,
        threadRepository,
        taskService,
        userRepository
    )
}
