package im.bigs.pg.external.pg.testpg.dto

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * TestPG 결제 승인 성공 응답.
 *
 * @property approvalCode 승인 코드
 * @property approvedAt 승인 시각 (UTC)
 * @property maskedCardLast4 카드 마지막 4자리
 * @property amount 결제 금액
 * @property status 승인 상태 (APPROVED)
 */
data class TestPgResponse(
    val approvalCode: String,
    val approvedAt: LocalDateTime,
    val maskedCardLast4: String,
    val amount: BigDecimal,
    val status: String
)
