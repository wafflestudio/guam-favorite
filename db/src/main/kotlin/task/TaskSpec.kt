package waffle.guam.task

import org.springframework.data.jpa.domain.Specification
import waffle.guam.image.ImageEntity
import waffle.guam.project.ProjectEntity
import waffle.guam.taskmessage.TaskMessageEntity
import waffle.guam.user.UserEntity
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.JoinType

object TaskSpec {
    fun all(): Specification<TaskEntity> = Specification { _, _, builder: CriteriaBuilder ->
        builder.conjunction()
    }

    fun fetchTaskMessages(): Specification<TaskEntity> = Specification { root, query, builder: CriteriaBuilder ->
        root.fetch<TaskEntity, Set<TaskMessageEntity>>("taskMessages", JoinType.LEFT)
        query.distinct(true)
        builder.conjunction()
    }

    fun fetchUser(): Specification<TaskEntity> = Specification { root, query, builder: CriteriaBuilder ->
        root.fetch<TaskEntity, UserEntity>("user", JoinType.INNER).run {
            fetch<UserEntity, ImageEntity>("image", JoinType.LEFT)
        }
        query.distinct(true)
        builder.conjunction()
    }

    fun fetchProject(): Specification<TaskEntity> = Specification { root, query, builder: CriteriaBuilder ->
        root.fetch<TaskEntity, ProjectEntity>("project", JoinType.INNER).run {
            fetch<ProjectEntity, ImageEntity>("thumbnail", JoinType.LEFT)
        }
        builder.conjunction()
    }

    fun userIds(userIds: List<Long>): Specification<TaskEntity> = Specification { root, _, builder ->
        root.get<UserEntity>("user").get<Long>("id").`in`(userIds)
    }

    fun projectIds(projectIds: List<Long>): Specification<TaskEntity> = Specification { root, _, builder ->
        root.get<Long>("projectId").`in`(projectIds)
    }

    fun userStates(userStates: List<String>): Specification<TaskEntity> = Specification { root, _, _ ->
        root.get<String>("userState").`in`(userStates)
    }

    fun positions(positions: List<String>): Specification<TaskEntity> = Specification { root, _, _ ->
        root.get<String>("position").`in`(positions)
    }
}
