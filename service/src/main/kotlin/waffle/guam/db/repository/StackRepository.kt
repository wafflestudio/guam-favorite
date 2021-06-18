package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.TechStackEntity

interface StackRepository : JpaRepository<TechStackEntity, Long>
