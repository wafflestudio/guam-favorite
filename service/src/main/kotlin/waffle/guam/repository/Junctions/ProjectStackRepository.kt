package waffle.guam.repository.Junctions

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.ProjectStack

interface ProjectStackRepository: JpaRepository< ProjectStack, Long> {
}