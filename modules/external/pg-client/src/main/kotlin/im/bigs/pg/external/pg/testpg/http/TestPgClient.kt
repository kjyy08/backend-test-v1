package im.bigs.pg.external.pg.testpg.http

import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.external.pg.common.http.PgHttpError
import im.bigs.pg.external.pg.common.http.PgRestClient
import im.bigs.pg.external.pg.testpg.config.TestPgConfig
import im.bigs.pg.external.pg.testpg.dto.TestPgEncryptedRequest
import im.bigs.pg.external.pg.testpg.dto.TestPgErrorCode
import im.bigs.pg.external.pg.testpg.dto.TestPgErrorResponse
import im.bigs.pg.external.pg.testpg.dto.TestPgRequest
import im.bigs.pg.external.pg.testpg.dto.TestPgResponse
import im.bigs.pg.external.pg.testpg.utils.Aes256GcmEncryptor
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class TestPgClient(
    private val pgRestClient: PgRestClient,
    private val testPgConfig: TestPgConfig,
    private val objectMapper: ObjectMapper
) : PgClientOutPort {

    companion object {
        private const val ENDPOINT = "/api/v1/pay/credit-card"
        private val log = LoggerFactory.getLogger(TestPgClient::class.java)
    }

    override fun supports(partnerId: Long): Boolean = partnerId == 2L

    override fun approve(request: PgApproveRequest): PgApproveResult {
        log.info(
            "TestPG 결제 승인 요청 - partnerId: {}, amount: {}, productName: {}",
            request.partnerId,
            request.amount,
            request.productName
        )

        // 평문 요청 데이터 생성
        val plaintextRequest = createPlainTextRequest(request)

        // JSON 직렬화
        val plaintextJson = objectMapper.writeValueAsString(plaintextRequest)

        // AES-256-GCM 암호화
        val encryptedValue = Aes256GcmEncryptor.encrypt(
            plaintext = plaintextJson,
            apiKey = testPgConfig.apiKey,
            ivBase64Url = testPgConfig.ivBase64Url
        )

        // 암호화된 요청 본문 생성
        val encryptedRequest = TestPgEncryptedRequest(encryptedValue)

        // HTTP POST 요청
        val response = executeHttpRequest(encryptedRequest)
        log.info("TestPG 결제 승인 성공 - approvalCode: {}", response.approvalCode)

        // PgApproveResult로 변환
        return convertToApproveResult(response)
    }

    /**
     * PgApproveRequest를 TestPG 평문 요청으로 변환합니다.
     */
    private fun createPlainTextRequest(request: PgApproveRequest): TestPgRequest {
        val cardNumber = generateTestCardNumber(request.cardBin!!, request.cardLast4!!)

        return TestPgRequest(
            cardNumber = cardNumber,
            birthDate = "19900101",
            expiry = "1227",
            password = "12",
            amount = request.amount
        )
    }

    /**
     * 테스트 카드 규칙에 맞게 테스트 카드 번호를 생성합니다.
     * - 성공: `cardBin`, `cardLast4` 모두 `1`로 이뤄진 경우
     * - 실패: `cardBin`, `cardLast4` 모두 `2`로 이뤄진 경우
     */
    private fun generateTestCardNumber(cardBin: String, cardLast4: String): String {
        return if (cardBin == "111111" && cardLast4 == "1111") {
            "${cardBin}111111${cardLast4}"
        } else {
            "${cardBin}222222${cardLast4}"
        }
    }

    /**
     * HTTP 요청을 실행하고 응답을 처리합니다.
     */
    private fun executeHttpRequest(encryptedRequest: TestPgEncryptedRequest): TestPgResponse {
        val url = "${testPgConfig.baseUrl}$ENDPOINT"

        return pgRestClient.post(
            url = url,
            headers = mapOf("API-KEY" to testPgConfig.apiKey),
            requestBody = encryptedRequest,
            responseType = TestPgResponse::class.java,
            onError = { error -> handleError(error) }
        )
    }

    /**
     * HTTP 에러를 처리합니다.
     */
    private fun handleError(error: PgHttpError): TestPgResponse {
        log.warn(
            "TestPG 결제 실패 - statusCode: {}, responseBody: {}",
            error.statusCode.value(),
            error.responseBody
        )

        when (error.statusCode) {
            HttpStatus.BAD_REQUEST -> {
                throw IllegalArgumentException("TestPG API 요청 실패: 잘못된 API 요청입니다.")
            }

            HttpStatus.UNAUTHORIZED -> {
                throw SecurityException("TestPG API 인증 실패: API-KEY가 유효하지 않습니다.")
            }

            HttpStatus.UNPROCESSABLE_ENTITY -> {
                val pgError = error.parseAs(TestPgErrorResponse::class.java)
                val errorCode = TestPgErrorCode.valueOf(pgError.errorCode)

                throw IllegalStateException("TestPG 결제 실패: [${errorCode.code}] ${errorCode.message}")
            }

            else -> {
                throw RuntimeException("TestPG API 에러: ${error.statusCode}")
            }
        }
    }

    /**
     * TestPG 응답을 PgApproveResult로 변환합니다.
     */
    private fun convertToApproveResult(response: TestPgResponse): PgApproveResult {
        return PgApproveResult(
            approvalCode = response.approvalCode,
            approvedAt = response.approvedAt,
            status = PaymentStatus.valueOf(response.status)
        )
    }
}