package waffle.guam.thread.command

sealed class GetThreadInfo : ThreadCommand {
    data class ByIds(
        val threadIds: List<Long>,
    ) : GetThreadInfo()

    data class ByUserIdAndProjectId(
        val userId: Long,
        val projectId: Long
    ) : GetThreadInfo()
}
