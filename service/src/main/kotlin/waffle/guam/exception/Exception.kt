package waffle.guam.exception

class DataNotFoundException(msg: String = "해당 데이터를 찾을 수 없습니다.") : RuntimeException(msg)

class InvalidRequestException(msg: String = "잘못된 요청입니다.") : RuntimeException(msg)

class JoinException(msg: String = "") : RuntimeException(msg)

class NotAllowedException(msg: String = "") : RuntimeException(msg)
