package waffle.guam.user.command

data class UpdateDevice(
    val deviceId: String
) : UserCommand
