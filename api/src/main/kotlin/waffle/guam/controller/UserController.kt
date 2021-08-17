package waffle.guam.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import waffle.guam.common.UserContext
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.model.User
import waffle.guam.service.UserService
import waffle.guam.service.command.UpdateFcmToken

@RequestMapping("user")
@RestController
class UserController(
    private val userService: UserService,
) {
    private val mapper = jacksonObjectMapper()

    @GetMapping("/me")
    fun getUser(
        userContext: UserContext
    ): SuccessResponse<User> =
        SuccessResponse(
            userService.get(userContext.id)
        )

    @GetMapping("/{id}")
    fun getUser(
        @PathVariable id: Long
    ): SuccessResponse<User> =
        SuccessResponse(userService.get(id))

    @PostMapping("")
    fun updateUser(
        @RequestParam("command") command: String,
        @RequestParam("imageFiles", required = false) file: MultipartFile?,
        userContext: UserContext
    ): SuccessResponse<User> =
        SuccessResponse(
            userService.update(
                command = mapper.readValue(command),
                image = file,
                userId = userContext.id
            )
        )

    @PostMapping("/fcm")
    fun updateUser(
        @RequestBody command: UpdateFcmToken,
        userContext: UserContext
    ): SuccessResponse<User> =
        SuccessResponse(
            userService.updateDeviceId(command, userContext.id)
        )

    @GetMapping("project/ids")
    fun getProjectIds(
        userContext: UserContext
    ): SuccessResponse<List<Long>> =
        SuccessResponse(
            data = userService.getProjectIds(userContext.id)
        )
}
