package im.bigs.pg.external.pg.common.http

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * PG API 호출을 위한 공통 HTTP 클라이언트.
 * - RestClient를 래핑하여 일관된 API 호출 방식을 제공합니다.
 * - 에러 핸들러를 통해 4xx/5xx 응답을 처리할 수 있습니다.
 */
@Component
class PgRestClient(
    private val restClient: RestClient
) {

    /**
     * POST 요청을 수행하고 응답을 지정된 타입으로 변환합니다.
     *
     * @param T 성공 응답 타입
     * @param url 요청 URL (전체 URL)
     * @param headers 요청 헤더 (선택, 기본값: 빈 맵)
     * @param requestBody 요청 본문 객체
     * @param responseType 성공 응답 타입 클래스
     * @param onError 에러 핸들러 (선택, PgHttpError를 받아 처리)
     * @return 응답 본문 (T 타입으로 역직렬화)
     * @throws PgHttpException 에러 핸들러가 없고 에러 발생 시
     * @throws IllegalStateException PG API가 null을 반환한 경우
     */
    fun <T> post(
        url: String,
        headers: Map<String, String> = emptyMap(),
        requestBody: Any,
        responseType: Class<T>,
        onError: ((PgHttpError) -> T)? = null
    ): T {
        val requestSpec = restClient.post()
            .uri(url)
            .body(requestBody)

        return executeRequest(requestSpec, headers, url, responseType, onError)
    }

    /**
     * GET 요청을 수행하고 응답을 지정된 타입으로 변환합니다.
     *
     * @param T 성공 응답 타입
     * @param url 요청 URL (전체 URL)
     * @param headers 요청 헤더 (선택, 기본값: 빈 맵)
     * @param responseType 성공 응답 타입 클래스
     * @param onError 에러 핸들러 (선택, PgHttpError를 받아 처리)
     * @return 응답 본문 (T 타입으로 역직렬화)
     * @throws PgHttpException 에러 핸들러가 없고 에러 발생 시
     * @throws IllegalStateException PG API가 null을 반환한 경우
     */
    fun <T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        responseType: Class<T>,
        onError: ((PgHttpError) -> T)? = null
    ): T {
        val requestSpec = restClient.get()
            .uri(url)

        return executeRequest(requestSpec, headers, url, responseType, onError)
    }

    /**
     * HTTP 요청을 실행하고 응답을 처리합니다.
     * 헤더 적용, 에러 핸들링, 응답 파싱을 모두 처리합니다.
     */
    private fun <T> executeRequest(
        requestSpec: RestClient.RequestHeadersSpec<*>,
        headers: Map<String, String>,
        url: String,
        responseType: Class<T>,
        onError: ((PgHttpError) -> T)? = null
    ): T {
        headers.forEach { (key, value) ->
            requestSpec.header(key, value)
        }

        val responseSpec = requestSpec.retrieve()

        if (onError != null) {
            responseSpec.onStatus({ it.isError }) { _, response ->
                val statusCode = response.statusCode
                val body = response.body.readAllBytes().toString(Charsets.UTF_8)
                throw PgHttpException(statusCode, body)
            }
        }

        return try {
            responseSpec.body(responseType)
                ?: throw IllegalStateException("PG API returned null response for URL: $url")
        } catch (e: PgHttpException) {
            if (onError != null) {
                onError(PgHttpError.of(e.statusCode, e.responseBody))
            } else {
                throw e
            }
        }
    }
}
