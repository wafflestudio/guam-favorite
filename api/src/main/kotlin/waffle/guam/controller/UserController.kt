package waffle.guam.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import waffle.guam.common.UserContext
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.model.User
import waffle.guam.service.TaskService
import waffle.guam.service.UserService
import waffle.guam.service.command.UpdateDevice
import waffle.guam.service.command.UpdateUser

@RequestMapping("user")
@RestController
class UserController(
    private val userService: UserService,
    private val taskService: TaskService
) {

    @GetMapping("")
    fun getUser(
        userContext: UserContext
    ): SuccessResponse<User> =
        SuccessResponse(
            userService.get(userContext.id)
        )

    @PostMapping("")
    fun updateUser(
        @RequestBody command: UpdateUser,
        userContext: UserContext
    ): SuccessResponse<User> =
        SuccessResponse(
            userService.update(command, userContext.id)
        )

    @PostMapping("/device")
    fun updateUser(
        @RequestBody command: UpdateDevice,
        userContext: UserContext
    ): SuccessResponse<User> =
        SuccessResponse(
            userService.updateDeviceId(command, userContext.id)
        )

    @PostMapping("image")
    fun updateImage(
        @RequestParam("image") multipartFile: MultipartFile,
        userContext: UserContext
    ): SuccessResponse<User> =
        SuccessResponse(
            userService.updateImage(multipartFile, userContext.id)
        )

    @DeleteMapping("image")
    fun deleteImage(
        userContext: UserContext
    ): SuccessResponse<User> =
        SuccessResponse(
            userService.deleteImage(userContext.id)
        )

    @GetMapping("project/ids")
    fun getProjectIds(
        userContext: UserContext
    ): SuccessResponse<List<Long>> =
        SuccessResponse(
            data = taskService.getProjectIds(userContext.id)
        )
}
