package waffle.guam.project

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest
import waffle.guam.project.command.SearchProject
import waffle.guam.project.model.Due
import waffle.guam.projectstack.ProjectStackService
import waffle.guam.task.TaskService

@DatabaseTest(["project/image.sql", "project/project.sql", "project/user.sql", "project/task.sql", "project/projectStack.sql"])
class ProjectServiceQueryTest @Autowired constructor(
    private val projectStackService: ProjectStackService,
    private val taskService: TaskService,
    private val projectRepository: ProjectRepository
) {

    val projectService: ProjectService = ProjectServiceImpl(
        projectStackService, taskService, projectRepository
    )

    @DisplayName("프로젝트 단순 조회")
    @Transactional
    @Test
    fun getSingleProject() {
        val result = projectService.getProject(1)

        result.techStacks shouldNotBe null
        result.techStacks!!.size shouldBe 3
    }

    @DisplayName("프로젝트 목록 조회")
    @Transactional
    @Test
    fun getAllProjects() {
        val result = projectService.getAllProjects(
            PageRequest.of(20, 20, Sort.by("modifiedAt").descending())
        )

        result.totalElements shouldBe 3
    }

    @DisplayName("프로젝트 검색_초성 검색")
    @Transactional
    @Test
    fun getSearchResults() {
        val result = projectService.getSearchResults(
            PageRequest.of(0, 20, Sort.by("modifiedAt").descending()),
            SearchProject(
                query = "ㅍㄹㅈ",
                due = null,
                position = null,
                stackId = null
            )
        )

        result.map {
            it.id shouldNotBe 2L
        }
    }

    /**
     *  듀가 되면 포지션도 된다.
     */
    @DisplayName("프로젝트 검색_듀")
    @Transactional
    @Test
    fun getSearchResultsByDue() {
        val result = projectService.getSearchResults(
            PageRequest.of(0, 20, Sort.by("modifiedAt").descending()),
            SearchProject(
                query = "",
                due = Due.SIX,
                position = null,
                stackId = null
            )
        )

        result.size shouldBe 1
        result.toList()[0].title shouldBe "바사아 ㅍㄹㅈㅌ"
    }

    @DisplayName("프로젝트 검색_스택")
    @Transactional
    @Test
    fun getSearchResultsByStackId() {
        val result = projectService.getSearchResults(
            PageRequest.of(0, 20, Sort.by("modifiedAt").descending()),
            SearchProject(
                query = "",
                due = null,
                position = null,
                stackId = 37
            )
        )

        result.size shouldBe 2
        result.map {
            it.id shouldNotBe 3L
        }
    }

    @DisplayName("프로젝트 마감 임박")
    @Transactional
    @Test
    fun getTabProjects() {
        val result = projectService.getTabProjects(
            PageRequest.of(0, 20, Sort.by("modifiedAt").descending())
        )

        result.toList()[0].id shouldBe 3L
    }
}