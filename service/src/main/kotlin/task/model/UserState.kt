package waffle.guam.task.model

enum class UserState {
    GUEST, MEMBER, LEADER, QUIT, DECLINED, CONTRIBUTED;

    fun isValidMember(): Boolean = this == GUEST || this == MEMBER || this == LEADER
}
