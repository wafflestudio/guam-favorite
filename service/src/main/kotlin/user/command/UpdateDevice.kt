package waffle.guam.user.command

data class UpdateDevice(
    val fcmToken: String
) : UserCommand
