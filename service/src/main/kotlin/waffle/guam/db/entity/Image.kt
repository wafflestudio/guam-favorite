
package waffle.guam.db.entity

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "images")
@Entity
data class ImageEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Enumerated(value = EnumType.STRING)
    val type: ImageType,

    val parentId: Long
) {
    fun getPath(): String = "${type.name}/$id"
}

enum class ImageType {
    PROFILE, PROJECT, THREAD, COMMENT
}
