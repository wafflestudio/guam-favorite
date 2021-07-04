package waffle.guam

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.data.repository.support.Repositories
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.UserEntity
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ThreadEntity
import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.TechStackEntity
import waffle.guam.db.entity.Status
import waffle.guam.db.entity.ImageType
import waffle.guam.db.repository.UserRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.StackRepository
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

    fun getUser(): UserEntity {
        val userRepository = repositories.getRepositoryFor(UserEntity::class.java).get() as UserRepository
        return userRepository.findById(1L).orElse(
            userRepository.save(DefaultDataInfo.user)
        )
    }

    fun getUserProfiles(): List<ImageEntity> {
        val imageRepository = repositories.getRepositoryFor(ImageEntity::class.java).get() as ImageRepository
        return imageRepository.findAll().let {
            if (it.isEmpty()) {
                imageRepository.saveAll(DefaultDataInfo.userProfiles)
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

    fun getThread(): ThreadEntity {
        val threadRepository = repositories.getRepositoryFor(ThreadEntity::class.java).get() as ThreadRepository
        return threadRepository.findById(1L).orElse(
            threadRepository.save(DefaultDataInfo.thread)
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

    val userProfiles = listOf(
        ImageEntity(url = "User1 ImageURL", parentId = 1, type = ImageType.USER_PROFILE),
        ImageEntity(url = "User2 ImageURL", parentId = 2, type = ImageType.USER_PROFILE),
    )

    val project = ProjectEntity(
        title = "Test Project",
        description = "Test Project Description",
        thumbnail = "Image",
        frontHeadcount = 0,
        backHeadcount = 0,
        designerHeadcount = 0,
    )

    val thread = ThreadEntity(
        projectId = 1,
        userId = 1,
        content = "Test Thread Content",
    )

    val images = listOf(
        ImageEntity(url = "User1 ImageURL", parentId = 1, type = ImageType.USER_PROFILE),
        ImageEntity(url = "User2 ImageURL", parentId = 2, type = ImageType.USER_PROFILE),
        ImageEntity(url = "thread1 ImageURL1", parentId = 1, type = ImageType.THREAD),
        ImageEntity(url = "thread1 ImageURL2", parentId = 1, type = ImageType.THREAD),
        ImageEntity(url = "thread1 ImageURL3", parentId = 1, type = ImageType.THREAD),
        ImageEntity(url = "thread2 ImageURL1", parentId = 2, type = ImageType.THREAD),
        ImageEntity(url = "thread2 ImageURL2", parentId = 2, type = ImageType.THREAD),
        ImageEntity(url = "comment1 ImageURL1", parentId = 1, type = ImageType.COMMENT),
        ImageEntity(url = "comment1 ImageURL2", parentId = 1, type = ImageType.COMMENT),
        ImageEntity(url = "comment1 ImageURL3", parentId = 1, type = ImageType.COMMENT),
        ImageEntity(url = "comment2 ImageURL1", parentId = 2, type = ImageType.COMMENT),
        ImageEntity(url = "comment2 ImageURL2", parentId = 2, type = ImageType.COMMENT),
    )

    val techStacks = listOf(
        TechStackEntity(name = "kotlin", aliases = "kotlin, 코틀린", thumbnail = ""),
        TechStackEntity(name = "python", aliases = "python, 파이썬", thumbnail = "")
    )
}
