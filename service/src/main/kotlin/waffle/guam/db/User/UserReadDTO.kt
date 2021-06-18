package waffle.guam.db.User

import com.fasterxml.jackson.annotation.JsonCreator

data class UserReadDTO (
    val id: Long,
    val name: String,
    val imageUrl: String?
){

}