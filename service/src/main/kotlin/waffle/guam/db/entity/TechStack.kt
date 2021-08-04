package waffle.guam.db.entity

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Table(name = "tech_stacks")
@Entity
data class TechStackEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    var name: String,

    var aliases: String,

    @OneToOne
    @JoinColumn(name = "thumbnail_id")
    var thumbnail: ImageEntity? = null,

    @Enumerated(EnumType.STRING)
    var position: Position
)
