package im.bigs.pg.application.payment.service

import im.bigs.pg.application.partner.port.out.FeePolicyOutPort
import im.bigs.pg.application.partner.port.out.PartnerOutPort
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.out.PaymentMetricsOutPort
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.partner.FeePolicy
import im.bigs.pg.domain.partner.Partner
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class PaymentServiceTest {
    private val partnerRepo = mockk<PartnerOutPort>()
    private val feeRepo = mockk<FeePolicyOutPort>()
    private val paymentRepo = mockk<PaymentOutPort>()
    private val paymentRegistry = object : PaymentMetricsOutPort {
        override fun recordValue(name: String, value: Double, tags: Map<String, String>) {}
    }
    private val pgClient = object : PgClientOutPort {
        override fun supports(partnerId: Long) = partnerId == 1L
        override fun approve(request: PgApproveRequest) =
            PgApproveResult(
                "APPROVAL-123",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                PaymentStatus.APPROVED
            )
    }

    @Test
    @DisplayName("결제 시 수수료 정책을 적용하고 저장해야 한다")
    fun `결제 시 수수료 정책을 적용하고 저장해야 한다`() {
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient), paymentRegistry)
        every { partnerRepo.findById(1L) } returns Partner(1L, "TEST", "Test", true)
        every { feeRepo.findEffectivePolicy(1L, any()) } returns FeePolicy(
            id = 10L,
            partnerId = 1L,
            effectiveFrom = LocalDateTime.ofInstant(
                Instant.parse("2020-01-01T00:00:00Z"),
                ZoneOffset.UTC
            ),
            percentage = BigDecimal("0.0300"),
            fixedFee = BigDecimal("100")
        )
        val savedSlot = slot<Payment>()
        every { paymentRepo.save(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 99L) }

        val cmd = PaymentCommand(partnerId = 1L, amount = BigDecimal("10000"), cardLast4 = "4242")
        val res = service.pay(cmd)

        assertEquals(99L, res.id)
        assertEquals(BigDecimal("400"), res.feeAmount)
        assertEquals(BigDecimal("9600"), res.netAmount)
        assertEquals(PaymentStatus.APPROVED, res.status)
    }

    @Test
    fun `존재하지 않는 Partner로 결제 시 예외가 발생해야 한다`() {
        // given
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient), paymentRegistry)

        val cmd = PaymentCommand(
            partnerId = 2L,
            amount = BigDecimal("10000"),
            cardLast4 = "4242"
        )

        every { partnerRepo.findById(2L) } returns null

        // when & then
        assertThatThrownBy { service.pay(cmd) }
            .isInstanceOf(NoSuchElementException::class.java)
    }

    @Test
    fun `비활성화된 Partner로 결제 시 예외가 발생해야 한다`() {
        // given
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient), paymentRegistry)

        val cmd = PaymentCommand(
            partnerId = 2L,
            amount = BigDecimal("10000"),
            cardLast4 = "4242"
        )

        every { partnerRepo.findById(2L) } returns Partner(2L, "TEST", "Test", false)

        // when & then
        assertThatThrownBy { service.pay(cmd) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `지원하는 PG Client가 없는 경우 예외가 발생해야 한다`() {
        // given
        val unSupportedPgClient = mockk<PgClientOutPort>()
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(unSupportedPgClient), paymentRegistry)

        val cmd = PaymentCommand(
            partnerId = 1L,
            amount = BigDecimal("10000"),
            cardLast4 = "4242"
        )

        every { unSupportedPgClient.supports(any()) } returns false
        every { partnerRepo.findById(1L) } returns Partner(1L, "TEST", "Test", true)

        // when & then
        assertThatThrownBy { service.pay(cmd) }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `수수료 정책을 찾을 수 없는 경우 예외가 발생해야 한다`() {
        // given
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient), paymentRegistry)

        val cmd = PaymentCommand(
            partnerId = 1L,
            amount = BigDecimal("10000"),
            cardLast4 = "4242"
        )

        every { partnerRepo.findById(1L) } returns Partner(1L, "TEST", "Test", true)
        every { feeRepo.findEffectivePolicy(1L, any()) } returns null

        // when & then
        assertThatThrownBy { service.pay(cmd) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `Partner별 정책에 따라 수수료 계산이 적용되어야 한다`() {
        // given
        val pgClient1 = mockk<PgClientOutPort>()
        every { pgClient1.supports(100L) } returns true
        every { pgClient1.supports(101L) } returns false
        every { pgClient1.approve(any()) } returns PgApproveResult(
            "APPROVAL-1",
            LocalDateTime.of(2024, 1, 1, 0, 0),
            PaymentStatus.APPROVED
        )

        val pgClient2 = mockk<PgClientOutPort>()
        every { pgClient2.supports(100L) } returns false
        every { pgClient2.supports(101L) } returns true
        every { pgClient2.approve(any()) } returns PgApproveResult(
            "APPROVAL-2",
            LocalDateTime.of(2024, 1, 1, 0, 0),
            PaymentStatus.APPROVED
        )

        val service =
            PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient1, pgClient2), paymentRegistry)

        every { partnerRepo.findById(100L) } returns Partner(100L, "PARTNER1", "파트너1", true)
        every { feeRepo.findEffectivePolicy(100L, any()) } returns FeePolicy(
            id = 1L,
            partnerId = 100L,
            effectiveFrom = LocalDateTime.of(2020, 1, 1, 0, 0),
            percentage = BigDecimal("0.0300"),
            fixedFee = BigDecimal("100")
        )

        every { partnerRepo.findById(101L) } returns Partner(101L, "PARTNER2", "파트너2", true)
        every { feeRepo.findEffectivePolicy(101L, any()) } returns FeePolicy(
            id = 2L,
            partnerId = 101L,
            effectiveFrom = LocalDateTime.of(2020, 1, 1, 0, 0),
            percentage = BigDecimal("0.0400"),
            fixedFee = BigDecimal("200")
        )

        val savedSlot = slot<Payment>()
        every { paymentRepo.save(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 100L) }
        every { paymentRepo.save(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 101L) }

        // when & then
        val amount = BigDecimal("10000")

        // payment1: 3% + 100원 적용
        val payment1 = service.pay(PaymentCommand(partnerId = 100L, amount = amount))

        assertEquals(BigDecimal("400"), payment1.feeAmount)
        assertEquals(BigDecimal("9600"), payment1.netAmount)
        assertEquals(BigDecimal("0.0300"), payment1.appliedFeeRate)

        // payment2: 4% + 200원 적용
        val payment2 = service.pay(PaymentCommand(partnerId = 101L, amount = amount))

        assertEquals(BigDecimal("600"), payment2.feeAmount)
        assertEquals(BigDecimal("9400"), payment2.netAmount)
        assertEquals(BigDecimal("0.0400"), payment2.appliedFeeRate)
    }
}
