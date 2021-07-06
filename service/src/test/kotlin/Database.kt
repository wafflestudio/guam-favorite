package waffle.guam

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.data.repository.support.Repositories
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.Due
import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.Status
import waffle.guam.db.entity.TechStackEntity
import waffle.guam.db.entity.CommentEntity
import waffle.guam.db.entity.ThreadEntity
import waffle.guam.db.entity.UserEntity
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.StackRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.UserRepository
import waffle.guam.db.repository.CommentRepository
import javax.persistence.EntityManager
import javax.persistence.Table

@Component
class Database(
    private val entityManager: EntityManager,
    beanFactory: ListableBeanFactory
) {
    private val tableNames: List<String> =
        entityManager.metamodel.entities.mapNotNull { it.javaType.getAnnotation(Table::class.java)?.name }

    private val repositories = Repositories(beanFactory)

    @Transactional
    fun cleanUp() {
        entityManager.flush()
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate()

        for (tableName in tableNames.toSet()) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate()
            entityManager.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1")
                .executeUpdate()
        }

        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate()
    }

    @Transactional
    fun flush() {
        entityManager.flush()
    }

    fun getUser(): UserEntity {
        val userRepository = repositories.getRepositoryFor(UserEntity::class.java).get() as UserRepository
        return userRepository.findById(1L).orElse(
            userRepository.save(DefaultDataInfo.user)
        )
    }

    fun getUsers(): List<UserEntity> {
        val userRepository = repositories.getRepositoryFor(UserEntity::class.java).get() as UserRepository
        return userRepository.findAll().let {
            if (it.isEmpty()) {
                userRepository.saveAll(DefaultDataInfo.users)
            } else {
                it
            }
        }
    }

    fun getProject(): ProjectEntity {
        val projectRepository = repositories.getRepositoryFor(ProjectEntity::class.java).get() as ProjectRepository
        return projectRepository.findById(1L).orElse(
            projectRepository.save(DefaultDataInfo.project)
        )
    }

// FIXME : 실행 불가: TaskRepository is in unnamed module of loader 'app'
//    fun getTask(): TaskEntity {
//        val taskRepository = repositories.getRepositoryFor(TaskEntity::class.java).get() as TaskRepository
//        return taskRepository.findById(1L).orElse(
//            taskRepository.save(DefaultDataInfo.task)
//        )
//    }

    fun getThread(): ThreadEntity {
        val threadRepository = repositories.getRepositoryFor(ThreadEntity::class.java).get() as ThreadRepository
        return threadRepository.findById(1L).orElse(
            threadRepository.save(DefaultDataInfo.thread)
        )
    }

    fun getComment(): CommentEntity {
        val commentRepository = repositories.getRepositoryFor(CommentEntity::class.java).get() as CommentRepository
        return commentRepository.findById(1L).orElse(
            commentRepository.save(DefaultDataInfo.comment)
        )
    }

    fun getImages(): List<ImageEntity> {
        val imageRepository = repositories.getRepositoryFor(ImageEntity::class.java).get() as ImageRepository
        return imageRepository.findAll().let {
            if (it.isEmpty()) {
                imageRepository.saveAll(DefaultDataInfo.images)
            } else {
                it
            }
        }
    }

    fun getTechStacks(): List<TechStackEntity> {
        val stackRepository = repositories.getRepositoryFor(TechStackEntity::class.java).get() as StackRepository

        return stackRepository.findAll().let {
            if (it.isEmpty()) {
                stackRepository.saveAll(DefaultDataInfo.techStacks)
            } else {
                it
            }
        }
    }
}

object DefaultDataInfo {
    val user = UserEntity(
        firebaseUid = "test",
        status = Status.ACTIVE,
        nickname = "jon",
        skills = "kotlin,python",
    )

    val users = listOf(
        UserEntity(
            firebaseUid = "test 1",
            nickname = "user1 nickname",
            image = ImageEntity(parentId = 1, type = ImageType.PROFILE)
        ),
        UserEntity(
            firebaseUid = "test 2",
            nickname = "user2 nickname",
            image = ImageEntity(parentId = 2, type = ImageType.PROFILE)
        ),
        UserEntity(firebaseUid = "test 3", nickname = "user3 nickname"),
    )

    val project = ProjectEntity(
        title = "Test Project",
        description = "Test Project Description",
        thumbnail = "Image",
        frontHeadcount = 0,
        backHeadcount = 0,
        designerHeadcount = 0,
        due = Due.SIX
    )

//    val task = TaskEntity(
//        position = Position.FRONTEND,
//        projectId = 1,
//        userId = 1,
//        state = State.MEMBER
//    )

    val thread = ThreadEntity(
        projectId = 1,
        userId = 1,
        content = "Test Thread Content",
    )

    val comment = CommentEntity(
        threadId = 1,
        userId = 1,
        content = "Test Thread Content",
    )

    val images = listOf(
        ImageEntity(parentId = 1, type = ImageType.PROFILE),
        ImageEntity(parentId = 1, type = ImageType.THREAD),
        ImageEntity(parentId = 1, type = ImageType.THREAD),
        ImageEntity(parentId = 1, type = ImageType.THREAD),
        ImageEntity(parentId = 2, type = ImageType.THREAD),
        ImageEntity(parentId = 2, type = ImageType.THREAD),
        ImageEntity(parentId = 1, type = ImageType.COMMENT),
        ImageEntity(parentId = 1, type = ImageType.COMMENT),
        ImageEntity(parentId = 1, type = ImageType.COMMENT),
        ImageEntity(parentId = 2, type = ImageType.COMMENT),
        ImageEntity(parentId = 2, type = ImageType.COMMENT),
    )

    val techStacks = listOf(
        TechStackEntity(name = "kotlin", aliases = "kotlin, 코틀린", thumbnail = "", position = Position.FRONTEND),
        TechStackEntity(name = "python", aliases = "python, 파이썬", thumbnail = "", position = Position.BACKEND)
    )
}
