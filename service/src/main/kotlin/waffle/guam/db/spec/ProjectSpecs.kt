package waffle.guam.db.spec

import org.springframework.data.jpa.domain.Specification
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ProjectStackView
import waffle.guam.db.entity.ProjectState
import waffle.guam.db.entity.ProjectView
import waffle.guam.db.entity.TaskView
import waffle.guam.db.entity.TechStackEntity
import waffle.guam.db.entity.UserEntity
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.JoinType

object ProjectSpecs {

    fun fetchJoinAll(ids: List<Long>): Specification<ProjectView> =
        Specification { root, query, builder: CriteriaBuilder ->
            root.fetch<ProjectView, Set<ProjectStackView>>("techStacks", JoinType.LEFT)
                .fetch<ProjectStackView, TechStackEntity>("techStack", JoinType.LEFT)
            root.fetch<ProjectView, Set<TaskView>>("tasks", JoinType.LEFT).let {
                it.fetch<TaskView, UserEntity>("user", JoinType.LEFT)
            }
            query.distinct(true)
            builder.`in`(root.get<Any>("id")).value(ids)
        }

    fun fetchJoinImminent(): Specification<ProjectEntity> =
        Specification { root, query, builder: CriteriaBuilder ->
            builder.and(
                builder.equal(root.get<Any>("state"), ProjectState.RECRUITING),
                builder.or(
                    builder.or(
                        builder.lessThan(root.get<Long>("frontHeadcount"), 2),
                        builder.lessThan(root.get<Long>("backHeadcount"), 2)
                    ),
                    builder.lessThan(root.get<Long>("designerHeadcount"), 2)
                )
            )
        }
}
