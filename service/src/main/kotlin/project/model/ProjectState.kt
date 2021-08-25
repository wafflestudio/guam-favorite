package waffle.guam.project.model

enum class ProjectState {
    RECRUITING, ONGOING, PENDING, CLOSED, COMPLETED;

    companion object {

        /**
         *  현재 작업현황을 보여주는 용도로 사용 ( user/prj/{ids} )
         */
        fun activeStates(): List<String> =
            listOf(ONGOING, RECRUITING).map { it.name }

        /**
         *  유저가 참여한 프로젝트를 보여주는 용 ( user/{id} )
         *  TODO 아카이브에 담길 프로젝트 상태 명확히 하기
         */
        fun archiveStates(): List<String> =
            listOf(ONGOING, RECRUITING, PENDING, COMPLETED).map { it.name }
    }
}
