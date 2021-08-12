package waffle.guam.stack

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface StackRepository : JpaRepository<StackEntity, Long> {

    @Query("select distinct s from StackEntity s left join fetch s.thumbnail t where s.id = :stackId")
    override fun findById(stackId: Long) : Optional<StackEntity>

    @Query("select distinct s from StackEntity s left join fetch s.thumbnail t where s.id in :ids")
    fun findAllByIds(ids: List<Long>): List<StackEntity>
}
