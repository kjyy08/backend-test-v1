package im.bigs.pg.external.pg.common.http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpStatusCode

/**
 * PG API HTTP 에러 정보.
 * 에러 핸들러로 전달됩니다.
 *
 * @property statusCode HTTP 상태 코드
 * @property responseBody 응답 본문 (JSON 문자열)
 */
data class PgHttpError(
    val statusCode: HttpStatusCode,
    val responseBody: String
) {
    companion object {
        fun of(statusCode: HttpStatusCode, responseBody: String) =
            PgHttpError(statusCode, responseBody)
    }

    /**
     * JSON 응답을 지정된 타입으로 파싱합니다.
     * @param T 에러 응답 타입
     * @param errorType 에러 응답 클래스
     * @return 파싱된 에러 객체
     */
    fun <T> parseAs(errorType: Class<T>): T {
        val objectMapper = jacksonObjectMapper()
        return objectMapper.readValue(responseBody, errorType)
    }
}
