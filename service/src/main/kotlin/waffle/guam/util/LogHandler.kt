package waffle.guam.util

import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object LogHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val logCache =
        Caffeine.newBuilder().maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.DAYS).build<String, List<String>>()

    fun info(msg: String) {
        logger.info(msg)
        MDC.get("request-id")?.let { addLog(it, msg, "INFO") }
    }

    fun error(e: Exception) {
        logger.error(e.stackTraceToString())
        MDC.get("request-id")?.let { addLog(it, e.stackTraceToString(), "ERROR") }
    }

    fun flushLogs(requestId: String): String? = logCache.getIfPresent(requestId)?.joinToString("\n")

    private fun addLog(requestId: String, msg: String, tag: String) {
        val list = logCache.getIfPresent(requestId) ?: emptyList()
        val msgWithExtraInfo =
            "[${LocalDateTime.ofInstant(Instant.now(), ZoneId.of("Asia/Seoul"))}] $tag [$requestId] $msg"
        logCache.put(requestId, list.plus(msgWithExtraInfo))
    }
}
