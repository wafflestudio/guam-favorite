package waffle.guam.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import waffle.guam.model.TechStack
import waffle.guam.service.StackService

@Controller
class StackController(
    private val stackService: StackService
) {

    @GetMapping("/stackinit")
    @ResponseBody
    fun createInitialStack() {
        stackService.init()
    }

    // C
    @PostMapping("/stack")
    @ResponseBody
    fun createStack(@RequestBody techStack: TechStack): Boolean {
        return stackService.create(techStack)
    }

    // R
    @GetMapping("/stacks")
    @ResponseBody
    fun getAllProjects(): List<TechStack> {
        return stackService.getAll()
    }

    // ** Hashtag Search, Filters DevType

    @GetMapping("/stacks/search")
    @ResponseBody
    fun findByName(@RequestParam query: String): List<TechStack> {
        return stackService.searchByKeyword(query)
    }

    // U
    // D
}
