package waffle.guam.project

import org.springframework.data.jpa.domain.Specification
import waffle.guam.image.ImageEntity
import waffle.guam.projectstack.ProjectStackEntity
import waffle.guam.stack.StackEntity
import waffle.guam.task.TaskEntity
import waffle.guam.user.UserEntity
import java.lang.RuntimeException
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

    fun dueLike(due: String): Specification<ProjectEntity> =
        Specification { root, _, _: CriteriaBuilder ->
            root.get<Any>("due").`in`(due)
        }

    fun stateLike(state: String): Specification<ProjectEntity> =
        Specification { root, _, _: CriteriaBuilder ->
            root.get<Any>("state").`in`(state)
        }

    fun positionLeft(position: String): Specification<ProjectEntity> =
        Specification { root, _, builder: CriteriaBuilder ->

            // TODO. Error Handling
            val rt =
                when (position) {
                    "FRONTEND" -> root.get<Int>("frontHeadcount")
                    "BACKEND" -> root.get<Int>("backHeadcount")
                    "DESIGNER" -> root.get<Int>("designerHeadcount")
                    else -> throw RuntimeException()
                }
            builder.ge(rt, 1)
        }

    fun fetchImminent(): Specification<ProjectEntity> =
        fetchImages().and { root, _, builder: CriteriaBuilder ->
            builder.and(
                builder.equal(root.get<Any>("state"), "RECRUITING"),
                builder.or(
                    builder.or(
                        builder.lessThan(root.get<Long>("frontHeadcount"), 2),
                        builder.lessThan(root.get<Long>("backHeadcount"), 2)
                    ),
                    builder.lessThan(root.get<Long>("designerHeadcount"), 2)
                )
            )
        }

    fun search(due: String?, position: String?): Specification<ProjectEntity> =
        fetchImages()
            .and(due?.let { dueLike(due) })
            .and(position?.let { positionLeft(position) })

    /**
     *  legacy codes : techStack, task -> fetch 없이 사용하기 위해 아예 엔티티에서 뺌. dependency injection 통해 다른 서비스에서 불러오도록 함.
     */

    fun fetchStacks(): Specification<ProjectEntity> =
        Specification { root, query, builder: CriteriaBuilder ->
            root.fetch<ProjectEntity, Set<ProjectStackEntity>>("techStacks", JoinType.LEFT)
            root.fetch<ProjectStackEntity, StackEntity>("techStack", JoinType.LEFT)
            query.distinct(true)
            builder.conjunction()
        }

    fun fetchJoinAll(): Specification<ProjectEntity> =
        Specification { root, query, builder: CriteriaBuilder ->
            root.fetch<ProjectEntity, Set<ProjectStackEntity>>("techStacks", JoinType.LEFT)
                .fetch<ProjectStackEntity, StackEntity>("techStack", JoinType.LEFT)
            root.fetch<ProjectEntity, Set<TaskEntity>>("tasks", JoinType.LEFT).let {
                it.fetch<TaskEntity, UserEntity>("user", JoinType.LEFT)
            }
            root.fetch<ProjectEntity, ImageEntity>("thumbnail", JoinType.LEFT)
            query.distinct(true)
            builder.conjunction()
        }

    fun oldSearch(due: String?, stackId: Long?, position: String?): Specification<ProjectEntity> {

        var builder =
            stateLike("RECRUITING").and { root, _, builder: CriteriaBuilder ->
                var predicate = builder.conjunction()
                stackId?.let {
                    val q = root
                        .join<ProjectEntity, ProjectStackEntity>("techStacks")
                        .join<ProjectStackEntity, StackEntity>("techStack")
                        .get<Any>("id")
                    predicate = builder.and(
                        builder.equal(q, stackId),
                        predicate
                    )
                }
                builder.conjunction()
            }

        position?.let {
            builder = builder.and(positionLeft(position))
        }
        due?.let {
            builder = builder.and(dueLike(due))
        }

        return builder
    }
}
