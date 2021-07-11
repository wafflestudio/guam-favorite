package waffle.guam

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import waffle.guam.db.entity.Due
import waffle.guam.service.ProjectService
import waffle.guam.service.UserService
import waffle.guam.service.command.CreateProject

@Service
class DataInitializer(
    private val userService: UserService,
    private val projectService: ProjectService
) {
    val client =
        WebClient.create("https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=AIzaSyAYoZLtqIgtE8eLeyNgCoLYIa3f3UYmXDs")

    val userMap = mapOf(
        "test@test.test" to "wafflestudio",
        "test2@test.test" to "wafflestudio",
        "test3@test.test" to "wafflestudio"
    )
    val protoCommand = CreateProject(
        title = "",
        description = "",
        thumbnail = null,
        frontLeftCnt = 3,
        backLeftCnt = 3,
        designLeftCnt = 3,
        techStackIds = listOf(),
        due = Due.UNDEFINED
    )

    suspend fun init() = runBlocking {
        val tokens: List<FirebaseTokenResponse> =
            userMap.entries.map {
                async {
                    client.post()
                        .bodyValue(FirebaseTokenRequest(it.key, it.value, true))
                        .retrieve()
                        .awaitBody<FirebaseTokenResponse>()
                }
            }.awaitAll()

        tokens.map { userService.getByFirebaseUid(it.idToken) }
            .map { user -> (1..3).map { protoCommand.copy(title = "${user.id}가 만든 ${it}번째 프로젝트") to user.id } }
            .flatten()
            .map { projectService.createProject(it.first, it.second) }
    }
}

data class FirebaseTokenRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean
)

data class FirebaseTokenResponse(
    val idToken: String
)
