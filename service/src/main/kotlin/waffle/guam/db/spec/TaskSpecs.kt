package waffle.guam.db.spec

import org.springframework.data.jpa.domain.Specification
import waffle.guam.db.entity.TaskMessage
import waffle.guam.db.entity.TaskStatus
import waffle.guam.db.entity.TaskView
import java.time.LocalDateTime
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.JoinType

object TaskSpecs {

    fun fetchJoinAll(): Specification<TaskView> =
        Specification { root, query, builder: CriteriaBuilder ->
            root.fetch<TaskView, Set<TaskMessage>>("tasks", JoinType.LEFT)
            query.distinct(true)
            query.orderBy(
                listOf(
                    builder.desc(
                        root.join<TaskView, Set<TaskMessage>>("tasks").get<TaskStatus>("status")
                    ),
                    builder.desc(
                        root.join<TaskView, Set<TaskMessage>>("tasks").get<LocalDateTime>("modifiedAt")
                    )
                )
            )
            builder.conjunction()
        }
}
