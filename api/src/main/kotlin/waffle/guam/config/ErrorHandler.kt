package waffle.guam.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import waffle.guam.DataNotFoundException
import waffle.guam.InvalidRequestException
import waffle.guam.JoinException
import waffle.guam.NotAllowedException
import waffle.guam.common.InvalidFirebaseTokenException

@ControllerAdvice
class ErrorHandler {
    @ExceptionHandler(value = [DataNotFoundException::class])
    fun notfound(e: RuntimeException) =
        ResponseEntity(ErrorResponse(e.message ?: ""), HttpStatus.NOT_FOUND)

    @ExceptionHandler(value = [InvalidFirebaseTokenException::class, InvalidRequestException::class])
    fun baeRequest(e: RuntimeException) =
        ResponseEntity(ErrorResponse(e.message ?: ""), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(value = [JoinException::class, NotAllowedException::class])
    fun notAllowed(e: RuntimeException) =
        ResponseEntity(ErrorResponse(e.message ?: ""), HttpStatus.FORBIDDEN)
}

data class ErrorResponse(
    val message: String
)
