package waffle.guam.stack

import waffle.guam.image.ImageEntity
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Table(name = "tech_stacks")
@Entity
data class StackEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    var name: String,

    var aliases: String,

    var position: String,

    @OneToOne
    @JoinColumn(name = "thumbnail_id")
    var thumbnail: ImageEntity? = null
)
