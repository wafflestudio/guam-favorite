package waffle.guam.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import waffle.guam.common.UserContext
import waffle.guam.controller.response.GuamResponse
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.service.UserService
import waffle.guam.service.command.UpdateUser

@RequestMapping("user")
@RestController
class UserController(
    private val userService: UserService
) {

    @GetMapping("")
    fun getUser(
        userContext: UserContext
    ): GuamResponse =
        SuccessResponse(
            userService.get(userContext.id)
        )

    @PostMapping("")
    fun updateUser(
        @RequestBody command: UpdateUser,
        userContext: UserContext
    ): GuamResponse =
        SuccessResponse(
            userService.update(command, userContext.id)
        )
}
