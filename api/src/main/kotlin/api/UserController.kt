package waffle.guam.api

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
import waffle.guam.api.request.UpdateFcmTokenRequest
import waffle.guam.api.request.UpdateUserRequest
import waffle.guam.api.response.SuccessResponse
import waffle.guam.api.response.UserResponse
import waffle.guam.common.UserContext
import waffle.guam.user.UserService
import waffle.guam.user.command.UserExtraInfo

@RequestMapping("user")
@RestController
class UserController(
    private val userService: UserService,
) {
    private val mapper = jacksonObjectMapper()

    @GetMapping("/me")
    fun getUser(
        userContext: UserContext
    ): SuccessResponse<UserResponse> =
        // TODO: 프로젝트 상태에 따른 필터
        SuccessResponse(
            UserResponse.of(
                userService.getUser(userId = userContext.id, extraInfo = UserExtraInfo(projects = true))
            )
        )

    /**
     * (1) select user
     * (2) select u, t, p from task t inner join user u outer join image i inner join project p outer join image i
     */

    @GetMapping("/{id}")
    fun getUser(
        @PathVariable id: Long
    ): SuccessResponse<UserResponse> =
        // TODO: 프로젝트 상태에 따른 필터
        SuccessResponse(
            UserResponse.of(
                userService.getUser(userId = id, extraInfo = UserExtraInfo(projects = true))
            )
        )

    /**
     * (1) select user
     * (2) select u, t, p from task t inner join user u outer join image i inner join project p outer join image i
     */

    @PostMapping("")
    fun updateUser(
        @RequestParam("command") request: String,
        @RequestParam("imageFiles", required = false) file: MultipartFile?,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        // FIXME: readValue deserialize 실패시 에러 핸들링
        userService.updateUser(
            userId = userContext.id,
            command = mapper.readValue(request, UpdateUserRequest::class.java).toCommand(file)
        ).let {
            SuccessResponse(Unit)
        }

    /**
     * 프로필 이미지 수정(프로필이 없는 상태에서 이미지 업로드)
     * (1) insert into image
     * (2) update user
     *
     * 프로필 이미지 수정(프로필이 있는 상태에서 이미지 삭제)
     * (1) select image
     * (2) delete image
     * (3) update user
     *
     * 프로필 이미지 수정(프로필이 있는 상태에서 이미지 업로드)
     * (1) insert inmage
     * (2) update user
     *
     * 프로필만 수정
     * (1) update user
     */

    @PostMapping("/fcm")
    fun updateUser(
        @RequestBody request: UpdateFcmTokenRequest,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        userService.updateDeviceToken(
            userId = userContext.id,
            fcmToken = request.fcmToken
        ).let {
            SuccessResponse(Unit)
        }

    /**
     * (1) update user
     */

    @GetMapping("project/ids")
    fun getProjectIds(
        userContext: UserContext
    ): SuccessResponse<List<Long>> =
        // TODO: 프로젝트 상태에 따른 필터
        userService.getUser(userId = userContext.id, extraInfo = UserExtraInfo(projects = true)).projects!!.run {
            SuccessResponse(
                data = map { it.projectId }
            )
        }

    /**
     * (1) select u, t, p from task t inner join user u outer join image i inner join project p outer join image i
     */
}
