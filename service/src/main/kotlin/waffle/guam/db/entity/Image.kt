package waffle.guam.db.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class ImageEntity(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    val id: Long,
    val type: ImageType,
    val parentId: Long,
    val url: String
)

enum class ImageType {
    USER_PROFILE, THREAD, COMMENT
}
