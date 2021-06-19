package waffle.guam.config

import com.google.firebase.auth.FirebaseAuth
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
import waffle.guam.service.UserService
import javax.servlet.http.HttpServletRequest

@Component
class UserContextResolver(
    private val sessionService: SessionService
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        UserContext::class.java.isAssignableFrom(parameter.parameterType)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): UserContext =
        (webRequest.nativeRequest as HttpServletRequest).getHeader(HttpHeaders.AUTHORIZATION)
            ?.let { sessionService.takeUserId(it) }
            ?: throw InvalidFirebaseTokenException()
}

interface SessionService {
    fun takeUserId(token: String): UserContext
}

@Service
class SessionServiceImpl(
    private val userService: UserService
) : SessionService {

    override fun takeUserId(token: String): UserContext =
        userService.getByFirebaseUid(firebaseUid = getFirebaseUid(token)).let {
            UserContext(it.id)
        }

    private fun getFirebaseUid(token: String): String =
        FirebaseAuth.getInstance().verifyIdToken(token).uid
}
