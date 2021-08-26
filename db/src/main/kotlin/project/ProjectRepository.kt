package waffle.guam.project

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface ProjectRepository : JpaRepository<ProjectEntity, Long>, JpaSpecificationExecutor<ProjectEntity> {

    @Query("select distinct p from ProjectEntity p left join fetch p.thumbnail t where p.id = :id")
    override fun findById(id: Long): Optional<ProjectEntity>

    @Query(
        "select distinct p from ProjectEntity p left join fetch p.thumbnail i where p.state = 'RECRUITING' and ( " +
            "  p.frontHeadcount - ( select count(*) from TaskEntity t where t.project.id = p.id and t.position = 'FRONTEND' and t.userState in ('MEMBER', 'LEADER') ) = 1 or " +
            "  p.backHeadcount - ( select count(*) from TaskEntity t where t.project.id = p.id and t.position = 'BACKEND' and t.userState in ('MEMBER', 'LEADER') ) = 1 or " +
            "  p.designerHeadcount - ( select count(*) from TaskEntity t where t.project.id = p.id and t.position = 'DESIGNER' and t.userState in ('MEMBER', 'LEADER') ) = 1  )" +
            "order by p.modifiedAt desc"
    )
    fun imminent(): List<ProjectEntity>

    @Query(
        "select distinct p from ProjectEntity p left join fetch p.thumbnail i " +
            "where p.due = :due and " +
            "  ( case when :position = 'FRONTEND' THEN p.frontHeadcount " +
            "           when :position = 'BACKEND' THEN p.backHeadcount " +
            "           when :position = 'DESIGNER' THEN p.designerHeadcount " +
            "           else 0 " +
            "       end ) > " +
            "  ( select count(*) from TaskEntity t where t.project.id = p.id and t.position = :position and t.userState in ('MEMBER', 'LEADER') ) " +
            "order by p.modifiedAt desc"
    )
    fun search(due: String, position: String): List<ProjectEntity>

    @Query(
        "select distinct p from ProjectEntity p left join fetch p.thumbnail i " +
            "    where  ( case when :position = 'FRONTEND' THEN p.frontHeadcount " +
            "           when :position = 'BACKEND' THEN p.backHeadcount " +
            "           when :position = 'DESIGNER' THEN p.designerHeadcount " +
            "           else 0 " +
            "       end )  > " +
            "  ( select count(*) from TaskEntity t  where t.project.id = p.id and t.position = :position and t.userState in ('MEMBER', 'LEADER') ) " +
            " order by p.modifiedAt desc "
    )
    fun search(position: String): List<ProjectEntity>
}
