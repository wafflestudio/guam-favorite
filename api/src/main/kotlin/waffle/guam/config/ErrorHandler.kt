package waffle.guam.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import waffle.guam.*
import waffle.guam.common.InvalidFirebaseTokenException

@ControllerAdvice
class ErrorHandler {
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

    @ExceptionHandler(value = [JoinException::class])
    fun joinExceptionOccurred(e: JoinException) =
        ResponseEntity(ErrorResponse(e.message ?: "join exception occurred"), e.code)
}

data class ErrorResponse(
    val message: String
)
