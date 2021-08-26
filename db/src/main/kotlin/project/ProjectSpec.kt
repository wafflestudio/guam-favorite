package waffle.guam.project

import org.springframework.data.jpa.domain.Specification
import waffle.guam.image.ImageEntity
import waffle.guam.task.TaskEntity
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.JoinType

object ProjectSpec {

    fun all(): Specification<ProjectEntity> =
        Specification { _, _, builder: CriteriaBuilder ->
            builder.conjunction()
        }

    fun fetchImages(): Specification<ProjectEntity> =
        Specification { root, query, builder: CriteriaBuilder ->
            root.join<ProjectEntity, ImageEntity>("thumbnail", JoinType.LEFT)
            query.distinct(true)
            builder.conjunction()
        }

    // TODO 유저 상태 관리 여기서 plain text 가지고 하지 말기
    fun fetchTasks(): Specification<ProjectEntity> =
        fetchImages().and { root, query, builder: CriteriaBuilder ->
            root.join<ProjectEntity, TaskEntity>("tasks", JoinType.LEFT).get<String>("userState").`in`(
                listOf("MEMBER", "LEADER")
            )
            query.distinct(true)
            builder.conjunction()
        }

    fun list(validStates: List<String>): Specification<ProjectEntity> =
        fetchImages().and { root, _, _: CriteriaBuilder ->
            root.get<String>("state").`in`(validStates)
        }

    fun dueLike(due: String): Specification<ProjectEntity> =
        Specification { root, _, _: CriteriaBuilder ->
            root.get<Any>("due").`in`(due)
        }

    fun stateLike(state: String): Specification<ProjectEntity> =
        Specification { root, _, _: CriteriaBuilder ->
            root.get<Any>("state").`in`(state)
        }

    fun search(due: String?): Specification<ProjectEntity> =
        fetchImages()
            .and(due?.let { dueLike(due) })
}
