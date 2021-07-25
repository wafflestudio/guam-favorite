package waffle.guam.db.spec

import org.springframework.data.jpa.domain.Specification
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ProjectStackView
import waffle.guam.db.entity.ProjectState
import waffle.guam.db.entity.ProjectView
import waffle.guam.db.entity.TaskView
import waffle.guam.db.entity.TechStackEntity
import waffle.guam.db.entity.UserEntity
import waffle.guam.db.entity.Due
import waffle.guam.db.entity.Position
import waffle.guam.exception.NotAllowedException
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.JoinType

object ProjectSpecs {

    fun fetchJoinAll(): Specification<ProjectView> =
        Specification { root, query, builder: CriteriaBuilder ->
            root.fetch<ProjectView, Set<ProjectStackView>>("techStacks", JoinType.LEFT)
                .fetch<ProjectStackView, TechStackEntity>("techStack", JoinType.LEFT)
            root.fetch<ProjectView, Set<TaskView>>("tasks", JoinType.LEFT).let {
                it.fetch<TaskView, UserEntity>("user", JoinType.LEFT)
            }
            query.distinct(true)
            builder.conjunction()
        }

    fun fetchJoinList(ids: List<Long>): Specification<ProjectView> =
        fetchJoinAll().and { root, _, builder: CriteriaBuilder ->
            builder.`in`(root.get<Any>("id")).value(ids)
        }


    fun search(due: Due?, stackId: Long?, position: Position?): Specification<ProjectView> =
        fetchJoinAll().and { root, query, builder: CriteriaBuilder ->
            var predicate = builder.conjunction()
            due?.let{
                predicate = builder.and(
                    builder.equal(root.get<Any>("due"), due),
                    predicate
                )
            }
            stackId?.let{
                val q = root
                    .join<ProjectView, ProjectStackView>("techStacks")
                    .join<ProjectStackView, TechStackEntity>("techStack")
                    .get<Any>("id")
                //TODO : 이거 맞나?
                predicate = builder.and(
                    builder.equal(q, stackId),
                    predicate
                )
            }
            position?.let{
                val rt =
                    when(it){
                        Position.FRONTEND -> root.get<Int>("frontHeadcount")
                        Position.BACKEND -> root.get<Int>("backHeadcount")
                        Position.DESIGNER -> root.get<Int>("designerHeadcount")
                        Position.WHATEVER -> throw NotAllowedException("Not reachable : 필터를 걸었는데 포지션이 상관 없는 경우")
                    }
                predicate = builder.and(
                    builder.ge(rt, 1),
                    predicate
                )
            }
            builder.and(
                predicate,
                builder.equal(root.get<Any>("state"), ProjectState.RECRUITING)
            )
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
