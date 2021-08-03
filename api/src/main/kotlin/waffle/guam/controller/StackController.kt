package waffle.guam.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import waffle.guam.model.TechStack
import waffle.guam.service.StackService
import waffle.guam.service.command.CreateUpdateStack

@Controller
class StackController(
    private val stackService: StackService
) {

    @GetMapping("/stackinit")
    fun createInitialStack() {
        stackService.init()
    }

    // C
    @PostMapping("/stack")
    fun createStack(
        @RequestBody command: CreateUpdateStack
    ): Boolean {
        return stackService.create(command)
    }

    // U
    @PutMapping("/stack/{stackId}")
    fun updateStack(
        @PathVariable stackId: Long,
        @RequestBody command: CreateUpdateStack
    ): Boolean {
        return stackService.update(stackId, command)
    }

    // R
    @GetMapping("/stacks")
    fun getAllProjects(): List<TechStack> {
        return stackService.getAll()
    }

    // ** Hashtag Search, Filters DevType

    @GetMapping("/stacks/search")
    fun findByName(@RequestParam query: String): List<TechStack> {
        return stackService.searchByKeyword(query)
    }

    // D
}
