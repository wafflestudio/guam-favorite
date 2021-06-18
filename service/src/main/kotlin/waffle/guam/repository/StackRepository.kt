package waffle.guam.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.DevType.TechStack

interface StackRepository: JpaRepository<TechStack, Long> {
}