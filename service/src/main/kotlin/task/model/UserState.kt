package waffle.guam.task.model

enum class UserState {
    GUEST, MEMBER, LEADER, QUIT, DECLINED;

    fun isValidMember(): Boolean = this == GUEST || this == MEMBER || this == LEADER
}
