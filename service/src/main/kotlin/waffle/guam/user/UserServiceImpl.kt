package waffle.guam.user

import waffle.guam.model.User
import waffle.guam.user.command.ExtraFieldParams
import waffle.guam.user.command.UpdateUser
import waffle.guam.user.event.DeviceUpdated
import waffle.guam.user.event.UserUpdated

class UserServiceImpl : UserService {
    override fun getUser(firebaseUid: String): User {
        TODO("Not yet implemented")
    }

    override fun getUser(userId: Long, extraFieldOptions: ExtraFieldParams): User {
        TODO("Not yet implemented")
    }

    override fun updateUser(userId: Long, command: UpdateUser): UserUpdated {
        TODO("Not yet implemented")
    }

    override fun updateDevice(userId: Long, deviceId: String): DeviceUpdated {
        TODO("Not yet implemented")
    }
}
