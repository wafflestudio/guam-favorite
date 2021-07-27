package waffle.guam.db.entity

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "tech_stacks")
@Entity
data class TechStackEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val name: String,

    val aliases: String,

    val thumbnail: String? = null,

    @Enumerated(EnumType.STRING)
    val position: Position
)
