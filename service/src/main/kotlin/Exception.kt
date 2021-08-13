package waffle.guam

import org.springframework.http.HttpStatus

class DataNotFoundException(msg: String = "해당 데이터를 찾을 수 없습니다.") : RuntimeException(msg)

class InvalidRequestException(msg: String = "잘못된 요청입니다.") : RuntimeException(msg)

class JoinException(msg: String, val code: HttpStatus = HttpStatus.BAD_REQUEST) : RuntimeException(msg)

class NotAllowedException(msg: String = "접근 권한이 없습니다.") : RuntimeException(msg)

class ConflictException(msg: String) : RuntimeException(msg)
