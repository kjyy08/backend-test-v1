package im.bigs.pg.external.pg.testpg.http

import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.external.pg.common.http.PgHttpError
import im.bigs.pg.external.pg.common.http.PgRestClient
import im.bigs.pg.external.pg.testpg.config.TestPgConfig
import im.bigs.pg.external.pg.testpg.dto.TestPgEncryptedRequest
import im.bigs.pg.external.pg.testpg.dto.TestPgErrorCode
import im.bigs.pg.external.pg.testpg.dto.TestPgErrorResponse
import im.bigs.pg.external.pg.testpg.dto.TestPgResponse
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.Test

class TestPgClientTest {
    private val pgRestClient = mockk<PgRestClient>()
    private val testPgConfig = TestPgConfig(
        baseUrl = "http://localhost:8080",
        apiKey = "test-api-key",
        ivBase64Url = "AAAAAAAAAAAAAAAA"
    )
    private val objectMapper = ObjectMapper()
    private val testPgClient = TestPgClient(pgRestClient, testPgConfig, objectMapper)

    @Test
    fun `partnerId가 2인 경우에만 동작해야 한다`() {
        assertThat(testPgClient.supports(2L)).isTrue()
        assertThat(testPgClient.supports(1L)).isFalse()
        assertThat(testPgClient.supports(3L)).isFalse()
        assertThat(testPgClient.supports(4L)).isFalse()
    }

    @Test
    fun `정상적인 요청이라면 결제 승인이 성공해야 한다`() {
        // given
        val request = PgApproveRequest(
            partnerId = 2L,
            amount = BigDecimal("10000"),
            cardBin = "111111",
            cardLast4 = "1111",
            productName = "테스트 상품"
        )

        val mockResponse = TestPgResponse(
            approvalCode = "APPROVAL123",
            approvedAt = LocalDateTime.now(),
            maskedCardLast4 = "1111",
            amount = BigDecimal("10000"),
            status = PaymentStatus.APPROVED.name
        )

        every {
            pgRestClient.post(
                url = any(),
                headers = mapOf("API-KEY" to testPgConfig.apiKey),
                requestBody = any<TestPgEncryptedRequest>(),
                responseType = TestPgResponse::class.java,
                onError = any()
            )
        } returns mockResponse

        // when
        val result = testPgClient.approve(request)

        // then
        assertThat(result.approvalCode).isEqualTo("APPROVAL123")
        assertThat(result.approvedAt).isNotNull
        assertThat(result.status).isEqualTo(PaymentStatus.APPROVED)
    }

    @Test
    fun `요청 파라미터가 잘못된 경우 적절한 예외가 발생해야 한다`() {
        // given
        val request = PgApproveRequest(
            partnerId = 2L,
            amount = BigDecimal("10001"),
            cardBin = "ab1123123zc41",
            cardLast4 = "2222",
            productName = "테스트 상품"
        )

        every {
            pgRestClient.post(
                url = any(),
                headers = any(),
                requestBody = any<TestPgEncryptedRequest>(),
                responseType = TestPgResponse::class.java,
                onError = any()
            )
        } answers {
            val onError = arg<(PgHttpError) -> TestPgResponse>(4)
            onError(PgHttpError.of(HttpStatus.BAD_REQUEST, "Invalid request"))
        }

        // when & then
        assertThatThrownBy { testPgClient.approve(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `API 키가 잘못된 경우 적절한 예외가 발생해야 한다`() {
        // given
        val request = PgApproveRequest(
            partnerId = 2L,
            amount = BigDecimal("10001"),
            cardBin = "222222",
            cardLast4 = "2222",
            productName = "테스트 상품"
        )

        every {
            pgRestClient.post(
                url = any(),
                headers = any(),
                requestBody = any<TestPgEncryptedRequest>(),
                responseType = TestPgResponse::class.java,
                onError = any()
            )
        } answers {
            val onError = arg<(PgHttpError) -> TestPgResponse>(4)
            onError(PgHttpError.of(HttpStatus.UNAUTHORIZED, "Unauthorized"))
        }

        // when & then
        assertThatThrownBy { testPgClient.approve(request) }
            .isInstanceOf(SecurityException::class.java)
    }

    @Test
    fun `결제에 실패한 경우 적절한 예외가 발생해야 한다`() {
        // given
        val request = PgApproveRequest(
            partnerId = 2L,
            amount = BigDecimal("10001"),
            cardBin = "222222",
            cardLast4 = "2222",
            productName = "테스트 상품"
        )

        val errorResponse = TestPgErrorResponse(
            TestPgErrorCode.INSUFFICIENT_LIMIT.code,
            TestPgErrorCode.INSUFFICIENT_LIMIT.name,
            TestPgErrorCode.INSUFFICIENT_LIMIT.message,
            "test-id"
        )

        every {
            pgRestClient.post(
                url = any(),
                headers = any(),
                requestBody = any<TestPgEncryptedRequest>(),
                responseType = TestPgResponse::class.java,
                onError = any()
            )
        } answers {
            val onError = arg<(PgHttpError) -> TestPgResponse>(4)
            onError(
                PgHttpError.of(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    objectMapper.writeValueAsString(errorResponse)
                )
            )
        }

        // when & then
        assertThatThrownBy { testPgClient.approve(request) }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `예상치 못한 HTTP 에러가 발생한 경우 적절한 예외가 발생해야 한다`() {
        // given
        val request = PgApproveRequest(
            partnerId = 2L,
            amount = BigDecimal("10001"),
            cardBin = "222222",
            cardLast4 = "2222",
            productName = "테스트 상품"
        )

        every {
            pgRestClient.post(
                url = any(),
                headers = any(),
                requestBody = any<TestPgEncryptedRequest>(),
                responseType = TestPgResponse::class.java,
                onError = any()
            )
        } answers {
            val onError = arg<(PgHttpError) -> TestPgResponse>(4)
            onError(PgHttpError.of(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"))
        }

        // when & then
        assertThatThrownBy { testPgClient.approve(request) }
            .isInstanceOf(RuntimeException::class.java)
    }
}
