package waffle.guam.service

import org.springframework.stereotype.Service
import waffle.guam.db.DevType.TechStack
import waffle.guam.db.DevType.TechStackDTO
import waffle.guam.db.Project.ProjectCreateDTO
import waffle.guam.db.Project.ProjectDTO
import waffle.guam.db.Project.ProjectReadDTO
import waffle.guam.db.ProjectStack
import waffle.guam.db.Task
import waffle.guam.db.User.UserReadDTO
import waffle.guam.repository.Junctions.ProjectStackRepository
import waffle.guam.repository.Junctions.ProjectUserRepository
import waffle.guam.repository.ProjectRepository

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val projectStackRepository: ProjectStackRepository,
    private val projectUserRepository: ProjectUserRepository,
    private val apiService: ApiService
) {
    private val searchEngine: SearchEngine = SearchEngine()


    // 1. create project - id, isRecruiting 제외, thumbnail not required
    //    thumbnail 없을 경우 그냥 empty string 주세요
    //    user 받아서 Task 상에 id 넣어주고 리턴
    fun createProject(p: ProjectCreateDTO, l:List<TechStack>, creatorId: Long): Boolean {
        val new = ProjectDTO().create(p).toEntity()
        val stackList: MutableList<ProjectStack> = ArrayList()
        val taskList: MutableList<Task> = ArrayList()
        l.map {
            stackList.add(ProjectStack( project = new, stack = it ))
        }
        taskList.add(Task( project_id = new , profile_id = creatorId ))
        new.techStacks.addAll(stackList)
        new.tasks.addAll(taskList)
        projectRepository.save(new)
        return true
    }

    fun getAllProjects(): List<ProjectReadDTO>{
        val allProjects = projectRepository.findAll()
        return allProjects.map { ProjectReadDTO.of(it) }
    }

    // 2. Id 사용 조회
    fun findProject(id: Long): ProjectReadDTO{

        val target = projectRepository.findById(id).get()
        val res = ProjectReadDTO.of(target)
        res.techStacks = target.techStacks.map { TechStackDTO.of( it.stack ) } as MutableList<TechStackDTO>

        val idList = target.tasks.map { it.profile_id }
        res.members = apiService.fetchUser(idList) as MutableList<UserReadDTO>

        return res
    }

    // 3. 마감 임박
    fun imminentProjects(): List<ProjectReadDTO> {
        return projectRepository.findImminent().map { ProjectReadDTO.of(it) }
    }

    fun searchByKeyword(query: String): List<ProjectReadDTO>{
        val map = mutableMapOf<ProjectReadDTO, Int>()

        val projects = getAllProjects()
        for (p in projects) {
            val a: MutableList<String> = ArrayList()
            a.add(p.title)
            a.add(p.description)
            val cnt = searchEngine.search(a, query)
            if( cnt > 0 ) map[p] = cnt
        }
        return map.toList().sortedWith(compareBy { -it.second }).map {it.first}
    }

    //U
    fun updateProject(id: Long, p: ProjectReadDTO, l: List<TechStack>): ProjectReadDTO {

        val target = (projectRepository.findById(id)).get()
        val new = ProjectDTO.of(target).update(p).toEntity()

        l.map {
            new.techStacks.add(ProjectStack( stack = it, project = new ))
        }

        projectRepository.save(new)
        return p
    }

    //D
    fun deleteProject(id: Long): Boolean{
        projectRepository.deleteById(id)
        return true
    }

}