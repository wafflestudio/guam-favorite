package waffle.guam.repository.Junctions

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.Task

interface ProjectUserRepository: JpaRepository<Task, Long> {
}