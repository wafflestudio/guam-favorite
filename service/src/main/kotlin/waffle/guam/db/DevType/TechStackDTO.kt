package waffle.guam.db.DevType

data class TechStackDTO (
    val name: String = "",
    val aliases: String = "",
    val thumbnail: String = ""
){
    companion object{
        fun of(e: TechStack): TechStackDTO{
            return TechStackDTO(e.name, e.aliases, e.thumbnail )
        }
    }

    fun toEntity(): TechStack {
        return TechStack( name = name, aliases = aliases, thumbnail = thumbnail )
    }
}