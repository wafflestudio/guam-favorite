package waffle.guam.projectstack.command

data class StackIdList(
    val front: Long?,
    val back: Long?,
    val design: Long?,
) {
    val validList: List<Long>
        get() = listOf(front, back, design).mapNotNull { it }

    val validCount: Int
        get() = validList.size
}
