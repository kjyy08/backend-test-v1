package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentPage
import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.application.payment.port.out.PaymentSummaryProjection
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QueryPaymentsServiceTest {
    private val paymentRepository = mockk<PaymentOutPort>()
    private val service = QueryPaymentsService(paymentRepository)

    @Test
    fun `페이지 조회 시 다음 페이지가 있으면 커서를 생성해야 한다`() {
        // given
        val filter = QueryFilter(partnerId = 1L, limit = 5)
        val testPayments = createTestPayments(5)
        val lastPayment = testPayments.last()

        every { paymentRepository.findBy(any()) } returns PaymentPage(
            items = testPayments,
            hasNext = true,
            nextCursorCreatedAt = lastPayment.createdAt,
            nextCursorId = lastPayment.id
        )

        every { paymentRepository.summary(any()) } returns PaymentSummaryProjection(
            count = 5L,
            totalAmount = BigDecimal("50000"),
            totalNetAmount = BigDecimal("48500")
        )

        // when
        val result = service.query(filter)
        val decoded = String(Base64.getUrlDecoder().decode(result.nextCursor))

        // then
        assertEquals(5, result.items.size)
        assertTrue(result.hasNext)
        assertNotNull(result.nextCursor)
        assertTrue(decoded.contains(":"))
    }

    @Test
    fun `마지막 페이지에서는 hasNext를 false로 반환해야 한다`() {
        // given
        val filter = QueryFilter(limit = 5)
        val testPayments = createTestPayments(5)

        every { paymentRepository.findBy(any()) } returns PaymentPage(
            items = testPayments,
            hasNext = false,
            nextCursorCreatedAt = null,
            nextCursorId = null
        )

        every { paymentRepository.summary(any()) } returns PaymentSummaryProjection(
            count = 5L,
            totalAmount = BigDecimal("50000"),
            totalNetAmount = BigDecimal("48500")
        )

        // when
        val result = service.query(filter)

        // then
        assertEquals(5, result.items.size)
        assertFalse(result.hasNext)
        assertNull(result.nextCursor)
    }

    @Test
    fun `잘못된 커서는 첫 페이지로 처리되어야 한다`() {
        // given
        val invalidCursor = "invalid-cursor-string"
        val filter = QueryFilter(cursor = invalidCursor)
        val querySlot = slot<PaymentQuery>()

        every { paymentRepository.findBy(capture(querySlot)) } returns PaymentPage(
            items = emptyList(),
            hasNext = false,
            nextCursorCreatedAt = null,
            nextCursorId = null
        )

        every { paymentRepository.summary(any()) } returns PaymentSummaryProjection(
            count = 0L,
            totalAmount = BigDecimal.ZERO,
            totalNetAmount = BigDecimal.ZERO
        )

        // when
        service.query(filter)

        // then
        assertNull(querySlot.captured.cursorCreatedAt)
        assertNull(querySlot.captured.cursorId)
    }

    @Test
    fun `summary는 동일한 필터링 조건으로 계산되어야 한다`() {
        // given
        val filter = QueryFilter(partnerId = 1L, status = PaymentStatus.APPROVED.name, limit = 10)

        every { paymentRepository.findBy(any()) } returns PaymentPage(
            items = createTestPayments(10),
            hasNext = true,
            nextCursorCreatedAt = LocalDateTime.now(),
            nextCursorId = 10L
        )

        every { paymentRepository.summary(any()) } returns PaymentSummaryProjection(
            count = 35L,
            totalAmount = BigDecimal("350000"),
            totalNetAmount = BigDecimal("339500")
        )

        // when
        val result = service.query(filter)

        // then
        verify {
            paymentRepository.summary(
                match {
                    it.partnerId == filter.partnerId && it.status?.name == filter.status
                }
            )
        }
        assertEquals(10, result.items.size)
        assertEquals(35L, result.summary.count)
    }

    private fun createTestPayments(count: Int): List<Payment> {
        return (1..count).map { i ->
            createPayment(
                id = i.toLong(),
                amount = BigDecimal(10000 + i.toLong()),
                createdAt = LocalDateTime.of(2024, 1, i, 0, 0)
            )
        }
    }

    private fun createPayment(
        id: Long = 1L,
        partnerId: Long = 1L,
        amount: BigDecimal = BigDecimal("10000"),
        createdAt: LocalDateTime = LocalDateTime.of(2024, 1, 1, 0, 0)
    ) = Payment(
        id = id,
        partnerId = partnerId,
        amount = amount,
        appliedFeeRate = BigDecimal("0.0300"),
        feeAmount = BigDecimal("400"),
        netAmount = BigDecimal("9600"),
        cardBin = null,
        cardLast4 = "1111",
        approvalCode = "code-$id",
        approvedAt = createdAt,
        status = PaymentStatus.APPROVED,
        createdAt = createdAt,
        updatedAt = createdAt
    )
}
