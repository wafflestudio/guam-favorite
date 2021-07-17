package waffle.guam.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service
import waffle.guam.db.repository.UserRepository

interface MessageService {
    fun sendMessage(ids: List<Long>, title: String, body: String)
}

@Service
class MessageServiceImpl(
    private val userRepository: UserRepository,
) : MessageService {
    override fun sendMessage(ids: List<Long>, title: String, body: String) {
        FirebaseMessaging.getInstance().sendMulticast(
            multicastMessage(
                targetTokens = userRepository.findAllById(ids).mapNotNull { it.deviceId },
                title = title,
                body = body
            )
        )
    }

    private fun multicastMessage(targetTokens: List<String>, title: String, body: String): MulticastMessage {
        return MulticastMessage.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .setImage(null)
                    .build()
            )
            .addAllTokens(targetTokens)
            .build()
    }
}
