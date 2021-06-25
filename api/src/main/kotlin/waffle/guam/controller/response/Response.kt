package waffle.guam.controller.response

sealed class GuamResponse

data class SuccessResponse<R>(
    val data: R
) : GuamResponse()

data class PageableResponse<R>(
    val data: List<R>,
    val size: Int,
    val offset: Int,
    val totalCount: Int,
    val hasNext: Boolean
) : GuamResponse()
