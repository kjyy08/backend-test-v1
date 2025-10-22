package im.bigs.pg.external.pg.testpg.dto

enum class TestPgErrorCode(
    val code: Int,
    val message: String
) {
    STOLEN_OR_LOST(1001, "도난 또는 분실된 카드입니다."),
    INSUFFICIENT_LIMIT(1002, "한도가 초과되었습니다."),
    EXPIRED_OR_BLOCKED(1003, "정지되었거나 만료된 카드입니다."),
    TAMPERED_CARD(1004, "위조 또는 변조된 카드입니다."),
    TAMPERED_CARD_NOT_ACCEPTED(1005, "위조 또는 변조된 카드입니다. (허용되지 않은 카드)")
}
