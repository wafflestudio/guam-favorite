package waffle.guam.controller.response

sealed class GuamResponse

data class SuccessResponse<R>(
    val data: R
) : GuamResponse()
