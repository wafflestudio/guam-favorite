package waffle.guam.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import waffle.guam.db.DevType.TechStackDTO
import waffle.guam.service.StackService

@Controller
class StackController(
    private val stackService: StackService
) {

    @GetMapping("/stackinit")
    @ResponseBody
    fun createInitialStack(){
        stackService.init()
    }

    // C
    @PostMapping("/stack")
    @ResponseBody
    fun createStack(@RequestBody techStackDTO: TechStackDTO): Boolean{
        return stackService.create(techStackDTO)
    }

    //R
    @GetMapping("/stacks")
    @ResponseBody
    fun getAllProjects(): List<TechStackDTO> {
        return stackService.getAll()
    }

    // ** Hashtag Search, Filters DevType

    @GetMapping("/stacks/search")
    @ResponseBody
    fun findByName(@RequestParam query: String): List<TechStackDTO> {
        return stackService.searchByKeyword(query)
    }

    //U
    //D
}