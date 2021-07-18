package waffle.guam.config

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import waffle.guam.common.InvalidFirebaseTokenException
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.exception.JoinException
import waffle.guam.exception.NotAllowedException

@ControllerAdvice
class ErrorHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(value = [DataNotFoundException::class])
    fun notfound(e: RuntimeException): ResponseEntity<ErrorResponse> {
        logger.error(e.message, e)
        return ResponseEntity(ErrorResponse(e.message ?: "", MDC.get("request-id")), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(value = [InvalidFirebaseTokenException::class, InvalidRequestException::class])
    fun baeRequest(e: RuntimeException): ResponseEntity<ErrorResponse> {
        logger.error(e.message, e)
        return ResponseEntity(ErrorResponse(e.message ?: "", MDC.get("request-id")), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(value = [JoinException::class, NotAllowedException::class])
    fun notAllowed(e: RuntimeException): ResponseEntity<ErrorResponse> {
        logger.error(e.message,e )
        return ResponseEntity(ErrorResponse(e.message ?: "", MDC.get("request-id")), HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [Exception::class])
    fun unknown(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error(e.message, e)
        return ResponseEntity(ErrorResponse(e.message ?: "알 수 없는 에러입니다.", MDC.get("request-id")), HttpStatus.FORBIDDEN)
    }
}

data class ErrorResponse(
    val message: String,
    val requestId: String?
)
