package waffle.guam.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import waffle.guam.db.repository.UserRepository

interface MessageService {
    fun sendMessage(ids: List<Long>, title: String, body: String)
}

@Service
class MessageServiceImpl(
    private val userRepository: UserRepository,
) : MessageService {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun sendMessage(ids: List<Long>, title: String, body: String) {
        val targetTokens = userRepository.findAllById(ids).mapNotNull { it.deviceId }.also {
            if (it.isEmpty()) {
                logger.info("No registered tokens from users[$ids].")
                return@sendMessage
            }
        }

        kotlin.runCatching {
            val result = FirebaseMessaging.getInstance().sendMulticast(
                multicastMessage(
                    targetTokens = targetTokens,
                    title = title,
                    body = body
                )
            )
            if (result.failureCount > 0) {
                result.responses.forEach {
                    logger.error("FCM failed", it)
                }
            }
        }.getOrElse {
            logger.error("FCM errored", it)
        }
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
