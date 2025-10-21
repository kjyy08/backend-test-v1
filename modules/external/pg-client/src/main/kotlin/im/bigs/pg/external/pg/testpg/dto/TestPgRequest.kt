package im.bigs.pg.external.pg.testpg.dto

import java.math.BigDecimal

data class TestPgRequest(
    val cardNumber: String,
    val birthDate: String,
    val expiry: String,
    val password: String,
    val amount: BigDecimal
)

data class TestPgEncryptedRequest(
    val enc: String
)
