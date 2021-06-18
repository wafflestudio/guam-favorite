package waffle.guam.db.User

import com.fasterxml.jackson.annotation.JsonCreator

data class UserDTO(
    val id: Long,
    val status: String,
    val name: String,
    val imageUrl: String?,
    val skills: List<String>?,
    val githubUrl: String?,
    val blogUrl: String?,
    val introduction: String?
){


}