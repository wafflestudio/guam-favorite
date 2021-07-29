package waffle.guam.user

import waffle.guam.model.User
import waffle.guam.user.command.UpdateUser
import waffle.guam.user.event.DeviceUpdated
import waffle.guam.user.event.UserUpdated

interface UserService {
    fun getUser(userId: Long): User
    fun getUser(firebaseUid: String): User
    fun updateUser(userId: Long, command: UpdateUser): UserUpdated
    fun updateDevice(userId: Long, deviceId: String): DeviceUpdated
}
