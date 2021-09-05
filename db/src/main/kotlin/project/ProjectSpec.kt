package waffle.guam.project

import org.springframework.data.jpa.domain.Specification
import waffle.guam.image.ImageEntity
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

    fun list(validStates: List<String>): Specification<ProjectEntity> =
        fetchImages().and { root, _, _: CriteriaBuilder ->
            root.get<String>("state").`in`(validStates)
        }

    fun dueLike(due: String): Specification<ProjectEntity> =
        Specification { root, _, _: CriteriaBuilder ->
            root.get<Any>("due").`in`(due)
        }

    fun stateLike(state: List<String>): Specification<ProjectEntity> =
        Specification { root, _, _: CriteriaBuilder ->
            root.get<Any>("state").`in`(state)
        }

    fun search(due: String? = null): Specification<ProjectEntity> =
        fetchImages()
            .and(due?.let { dueLike(due) })
            .and(stateLike(listOf("RECRUITING", "ONGOING")))
}
