package waffle.guam.image

import org.springframework.data.jpa.repository.JpaRepository

interface ImageRepository : JpaRepository<ImageEntity, Long> {
    fun findByParentIdAndType(parentId: Long, type: String): List<ImageEntity>
}
