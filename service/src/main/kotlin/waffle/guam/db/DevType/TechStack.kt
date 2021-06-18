package waffle.guam.db.DevType

import javax.persistence.*

@Table(name = "techstacks")
@Entity
data class TechStack(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val name: String = "",
    val aliases: String = "",
    val thumbnail: String = ""
){


}
