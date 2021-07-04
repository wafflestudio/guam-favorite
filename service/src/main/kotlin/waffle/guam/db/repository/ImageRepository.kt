package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.ImageType

interface ImageRepository : JpaRepository<ImageEntity, Long> {
    fun findByParentIdAndType(parentId: Long, type: ImageType): List<ImageEntity>

    fun countByParentIdAndType(parentId: Long, type: ImageType): Long

    fun deleteByParentIdAndType(parentId: Long, type: ImageType)

    fun deleteByParentIdInAndType(parentId: List<Long>, type: ImageType): List<ImageEntity>
}