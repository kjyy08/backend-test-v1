package im.bigs.pg.external.pg.testpg.dto

/**
 * TestPG 결제 실패 응답 (422 Unprocessable Entity).
 *
 * @property code 에러 코드 정수 (1001~1005)
 * @property errorCode 실패 사유 코드 (예: INSUFFICIENT_LIMIT)
 * @property message 실패 메시지 (예: "한도가 초과되었습니다.")
 * @property referenceId 참조 ID (UUID)
 */
data class TestPgErrorResponse(
    val code: Int,
    val errorCode: String,
    val message: String,
    val referenceId: String
)
