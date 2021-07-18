package waffle.guam.config

import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.servlet.HandlerInterceptor
import waffle.guam.util.LogHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class LogInterceptor : HandlerInterceptor {
    private val webClient = WebClient.create("http://13.209.157.42:8080")

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        MDC.get("request-id")?.let { reqId ->
            LogHandler.flushLogs(reqId)?.let {
                sendLogs(it)
            }
        }
        super.afterCompletion(request, response, handler, ex)
    }

    private fun sendLogs(logs: String) {
        webClient.post()
            .uri("/guam")
            .bodyValue(LogInfo(logs))
            .retrieve()
            .bodyToMono<String>()
            .subscribe { println(it) }
    }

    private data class LogInfo(val msg: String)
}
