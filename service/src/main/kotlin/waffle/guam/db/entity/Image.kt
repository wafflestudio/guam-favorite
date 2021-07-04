package waffle.guam.db.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class ImageEntity(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    val id: Long = 0L,

    val type: ImageType,

    val parentId: Long
) {
    val path: String = "${type.name}/$parentId"
}

enum class ImageType {
    PROFILE, PROJECT, THREAD, COMMENT
}
