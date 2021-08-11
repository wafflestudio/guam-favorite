package waffle.guam.stack

import org.springframework.data.jpa.repository.JpaRepository

interface StackRepository : JpaRepository<StackEntity, Long>
