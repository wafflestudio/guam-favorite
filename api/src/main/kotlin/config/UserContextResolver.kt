package waffle.guam.config

import com.google.firebase.auth.FirebaseAuthException
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import waffle.guam.common.InvalidFirebaseTokenException
import waffle.guam.common.UserContext
import waffle.guam.user.UserService
import javax.servlet.http.HttpServletRequest

@Component
class UserContextResolver(
    private val sessionService: SessionService,
) : HandlerMethodArgumentResolver {

    private val logger = LoggerFactory.getLogger(this::javaClass.name)

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        UserContext::class.java.isAssignableFrom(parameter.parameterType)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): UserContext {
        val req = (webRequest.nativeRequest as HttpServletRequest)
        val userContext = req.getHeader(HttpHeaders.AUTHORIZATION)?.let {
            kotlin.runCatching {
                sessionService.takeUserId(it)
            }.getOrElse {
                if (it is FirebaseAuthException && it.message?.contains("expired") == true) {
                    throw InvalidFirebaseTokenException("만료된 토큰입니다.")
                }
                throw InvalidFirebaseTokenException("잘못된 토큰입니다.")
            }
        } ?: throw InvalidFirebaseTokenException("토큰 정보를 찾을 수 없습니다.")

        logger.info("[USER-${userContext.id}] ${req.method} : ${req.requestURI}")

        return userContext
    }
}

interface SessionService {
    fun takeUserId(token: String): UserContext
}

@Service
class SessionServiceImpl(
    private val userService: UserService,
) : SessionService {

    override fun takeUserId(token: String): UserContext =
        UserContext(userService.getUserId(firebaseUid = token))

    // TODO: impl real authentication

    private fun getFirebaseUid(token: String): String {
        TODO()
    }
}
