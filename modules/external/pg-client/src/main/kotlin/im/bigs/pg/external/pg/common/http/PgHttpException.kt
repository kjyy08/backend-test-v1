package im.bigs.pg.external.pg.common.http

import org.springframework.http.HttpStatusCode

/**
 * PG API HTTP 에러 예외.
 * 4xx/5xx 응답 시 발생합니다.
 *
 * @property statusCode HTTP 상태 코드
 * @property responseBody 응답 본문 (JSON 문자열)
 */
class PgHttpException(
    val statusCode: HttpStatusCode,
    val responseBody: String
) : RuntimeException("PG API error: $statusCode, body: $responseBody")
