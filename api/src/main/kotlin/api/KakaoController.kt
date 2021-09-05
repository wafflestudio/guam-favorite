package waffle.guam.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/kakao")
class KakaoController {
    private val webClient: WebClient = WebClient.create("https://kapi.kakao.com/v2/user/me")
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @GetMapping("")
    fun getFirebaseToken(
        @RequestParam token: String,
    ): Mono<Response> =
        webClient.get()
            .headers { it.set("Authorization", "Bearer $token") }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(RequestMeResponse::class.java)
            .map { res ->
                val firebaseUid =
                    getUserWithUid(res.id) ?: FirebaseAuth.getInstance().createUser(
                        UserRecord.CreateRequest().also { it.setUid("refactor:${res.id}") }
                    ).uid

                Response(
                    customToken = FirebaseAuth.getInstance().createCustomToken(firebaseUid)
                )
            }

    fun getUserWithUid(uid: Int): String? =
        kotlin.runCatching { FirebaseAuth.getInstance().getUser("refactor:$uid").uid }.let {
            if (it.isFailure) {
                logger.error("Get firebaseUser failed with uid [refactor:$uid]", it.exceptionOrNull())
                null
            } else {
                it.getOrNull()!!
            }
        }

    data class Response(
        val customToken: String,
    )
}

private data class RequestMeResponse(
    val connected_at: String?,
    val id: Int,
    val kakao_account: KakaoAccount,
    val properties: Properties?,
)

private data class KakaoAccount(
    val age_range: String?,
    val age_range_needs_agreement: Boolean?,
    val birthday: String?,
    val birthday_needs_agreement: Boolean?,
    val birthday_type: String?,
    val email: String?,
    val email_needs_agreement: Boolean?,
    val gender_needs_agreement: Boolean?,
    val has_age_range: Boolean,
    val has_birthday: Boolean,
    val has_email: Boolean,
    val has_gender: Boolean,
    val is_email_valid: Boolean,
    val is_email_verified: Boolean,
    val profile: Profile?,
    val profile_needs_agreement: Boolean?,
)

private data class Profile(
    val is_default_image: Boolean,
    val nickname: String,
    val profile_image_url: String,
    val thumbnail_image_url: String,
)

private data class Properties(
    val nickname: String,
    val profile_image: String,
    val thumbnail_image: String,
)
