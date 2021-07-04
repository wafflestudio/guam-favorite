package waffle.guam.db.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.GenerationType

@Table(name = "images")
@Entity
data class ImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val type: ImageType,

    val parentId: Long,

    val url: String
)

enum class ImageType {
    USER_PROFILE, THREAD, COMMENT
}
