package waffle.guam.user

interface UserActionFilter {
    fun tryCreateProject(userId: Long)
    fun tryUpdateProject(userId: Long, projectId: Long)
    fun tryJoinProject(userId: Long, projectId: Long)
    fun tryQuitProject(userId: Long, projectId: Long)
    fun tryDeleteProject(userId: Long, projectId: Long)
}
