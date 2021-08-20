package waffle.guam.task.model

enum class UserState {
    GUEST, MEMBER, LEADER, DECLINED, LEFT, CANCELED, CONTRIBUTED;

    companion object {
        val validStates: List<UserState> = listOf(GUEST, MEMBER, LEADER)
        val officialStates: List<UserState> = listOf(MEMBER, LEADER)
    }

    fun isValidState(): Boolean = validStates.contains(this)
    fun isOfficialState(): Boolean = officialStates.contains(this)
}
