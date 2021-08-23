package waffle.guam.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import waffle.guam.api.response.StackResponse
import waffle.guam.api.response.SuccessResponse
import waffle.guam.stack.StackService

@RestController
@RequestMapping
class StackController(
    val stackService: StackService
) {

    @GetMapping("/stacks")
    fun getAllStacks(): SuccessResponse<List<StackResponse>> =
        stackService.getAllStacks().map {
            StackResponse.of(it)
        }.run {
            SuccessResponse(this)
        }
}
