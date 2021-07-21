package waffle.guam.db.spec

import org.springframework.data.jpa.domain.Specification
import waffle.guam.db.entity.ProjectStackView
import waffle.guam.db.entity.ProjectView
import waffle.guam.db.entity.TaskView
import waffle.guam.db.entity.TechStackEntity
import waffle.guam.db.entity.UserEntity
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.JoinType

class ProjectSpecs() {

    companion object {
        fun fetchJoinAll(): Specification<ProjectView> =
            Specification { root, query, builder: CriteriaBuilder ->
                root.fetch<ProjectView, Set<ProjectStackView>>("techStacks", JoinType.LEFT)
                    .fetch<ProjectStackView, TechStackEntity>("techStack", JoinType.LEFT)
                root.fetch<ProjectView, Set<TaskView>>("tasks", JoinType.LEFT).let {
                    it.fetch<TaskView, UserEntity>("user", JoinType.LEFT)
                }
                builder.conjunction()
            }
    }
}
