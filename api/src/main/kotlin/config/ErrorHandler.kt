package waffle.guam.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import waffle.guam.ConflictException
import waffle.guam.DataNotFoundException
import waffle.guam.InvalidRequestException
import waffle.guam.NotAllowedException
import waffle.guam.common.InvalidFirebaseTokenException

@ControllerAdvice
class ErrorHandler {
    private val logger = LoggerFactory.getLogger(this::javaClass.name)

    @ExceptionHandler(value = [DataNotFoundException::class])
    fun notfound(e: RuntimeException) =
        ResponseEntity(ErrorResponse(e.message ?: "404 not found"), HttpStatus.NOT_FOUND)

    @ExceptionHandler(value = [InvalidFirebaseTokenException::class, InvalidRequestException::class])
    fun badRequest(e: RuntimeException) =
        ResponseEntity(ErrorResponse(e.message ?: "400 bad request"), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(value = [NotAllowedException::class])
    fun notAllowed(e: RuntimeException) =
        ResponseEntity(ErrorResponse(e.message ?: "403 forbidden"), HttpStatus.FORBIDDEN)

    @ExceptionHandler(value = [ConflictException::class])
    fun conflict(e: RuntimeException) =
        ResponseEntity(ErrorResponse(e.message ?: "409 conflict"), HttpStatus.CONFLICT)

    //  @ExceptionHandler(value = [JoinException::class])
    //  fun joinExceptionOccurred(e: JoinException) =
    //      ResponseEntity(ErrorResponse(e.message ?: "참가 과정에서 문제가 발생하였습니다."), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(value = [Exception::class])
    fun internalError(e: Exception) =
        ResponseEntity(ErrorResponse(e.message ?: "알 수 없는 문제가 발생하였습니다."), HttpStatus.INTERNAL_SERVER_ERROR).also {
            logger.error("", e)
        }
}

data class ErrorResponse(
    val message: String
)
