package waffle.guam.controller

import org.springframework.web.bind.annotation.*
import waffle.guam.db.DevType.TechStack
import waffle.guam.db.DevType.TechStackDTO
import waffle.guam.db.Project.*
import waffle.guam.service.ApiService
import waffle.guam.service.ProjectService
import waffle.guam.service.StackService

@RestController
@RequestMapping()
class ProjectController(
    private val projectService: ProjectService,
    private val stackService: StackService,
    private val apiService: ApiService
) {

    // 1. Create Project
    @PostMapping("/projects")
    @ResponseBody
    fun createProject(@RequestBody projectCreateDTO: ProjectCreateDTO, @RequestHeader("USER-ID") id: Long): Boolean{
        val devIdList = projectCreateDTO.techStacks.map { stackService.searchIdByDTO(it) }
        val devList = devIdList.map {stackService.searchById(it) }
        return projectService.createProject(projectCreateDTO, devList, id)
    }

    @GetMapping("/projects")
    @ResponseBody
    fun getAllProjects(): List<ProjectReadDTO> {
        return projectService.getAllProjects()
    }

    // 2. Find Project by Id
    @GetMapping("/projects/{id}")
    @ResponseBody
    fun findProject(@PathVariable id: Long): ProjectReadDTO {
        return projectService.findProject(id)
    }

    // 3. 마감 임박 프로젝트 목록
    @GetMapping("/projects/tab")
    fun imminentProjects(): List<ProjectReadDTO>{
        return projectService.imminentProjects()
    }

    // 4. 프로젝트 검색 - 활동기간? 잔여 포지션??
    @GetMapping("/projects/search")
    @ResponseBody
    fun searchProject(@RequestParam keyword: String, @RequestBody(required=false) body: Pair<Int, List<TechStackDTO>>?): List<ProjectReadDTO> {
        return projectService.searchByKeyword(keyword)
    }

    //U
    @PutMapping("/projects/{id}")
    @ResponseBody
    fun updateProject(@PathVariable id: Long, @RequestBody projectReadDTO: ProjectReadDTO): ProjectReadDTO{
        val devIdList = projectReadDTO.techStacks.map { stackService.searchIdByDTO(it) }
        val devList = devIdList.map {stackService.searchById(it) }
        return projectService.updateProject(id, projectReadDTO, devList)
    }

    //D
    @DeleteMapping("/projects/{id}")
    @ResponseBody
    fun deleteProject(@PathVariable id: Long): Boolean {
        return projectService.deleteProject(id)
    }

}

