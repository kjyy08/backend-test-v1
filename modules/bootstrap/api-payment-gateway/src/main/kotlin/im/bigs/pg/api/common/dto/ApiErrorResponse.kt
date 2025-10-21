package im.bigs.pg.api.common.dto

data class ApiErrorResponse(
    val success: Boolean,
    val status: Int,
    val message: String
) {
    companion object {
        fun of(status: Int, message: String) = ApiErrorResponse(
            success = false,
            status = status,
            message = message
        )
    }
}
