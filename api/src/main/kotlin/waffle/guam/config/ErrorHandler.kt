package waffle.guam.config

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
    @ExceptionHandler(value = [DataNotFoundException::class])
    fun notfound(e: RuntimeException) =
        ResponseEntity(ErrorResponse(e.message ?: "데이터를 찾지 못하였습니다"), HttpStatus.NOT_FOUND)

    @ExceptionHandler(value = [InvalidFirebaseTokenException::class, InvalidRequestException::class])
    fun badRequest(e: RuntimeException) =
        ResponseEntity(ErrorResponse(e.message ?: "잘못된 요청입니다"), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(value = [JoinException::class, NotAllowedException::class])
    fun notAllowed(e: RuntimeException) =
        ResponseEntity(ErrorResponse(e.message ?: "권한이 없습니다"), HttpStatus.FORBIDDEN)
}

data class ErrorResponse(
    val message: String
)
