package waffle.guam.project

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface ProjectRepository : JpaRepository<ProjectEntity, Long>, JpaSpecificationExecutor<ProjectEntity> {

    @Query("select distinct p from ProjectEntity p left join fetch p.thumbnail t where p.id = :id")
    override fun findById(id: Long): Optional<ProjectEntity>
}
