package waffle.guam.user

import waffle.guam.user.command.UpdateUser
import waffle.guam.user.command.UserExtraFieldParams
import waffle.guam.user.event.DeviceUpdated
import waffle.guam.user.event.UserUpdated
import waffle.guam.user.model.User

interface UserService {
    fun getUser(userId: Long, extraFieldOptions: UserExtraFieldParams = UserExtraFieldParams()): User
    fun updateUser(userId: Long, command: UpdateUser): UserUpdated
    fun updateDeviceToken(userId: Long, fcmToken: String): DeviceUpdated
}
