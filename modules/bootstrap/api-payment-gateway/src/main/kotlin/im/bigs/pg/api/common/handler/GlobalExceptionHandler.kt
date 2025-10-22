package im.bigs.pg.api.common.handler

import im.bigs.pg.api.common.dto.ApiErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    /**
     * 잘못된 파라미터 요청 (400 Bad Request)
     * - 입력값 검증 실패
     * - 필수 파라미터 누락
     * - 형식 오류
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiErrorResponse> {
        log.warn(ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), ex.message ?: "잘못된 요청입니다"))
    }

    /**
     * 인증 실패 (401 Unauthorized)
     * - API-KEY 오류
     * - 토큰 만료
     * - 인증 정보 없음
     */
    @ExceptionHandler(SecurityException::class)
    fun handleSecurityException(ex: SecurityException): ResponseEntity<ApiErrorResponse> {
        log.error(ex.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), ex.message ?: "인증에 실패했습니다"))
    }

    /**
     * 리소스 없음 (404 Not Found)
     * - 존재하지 않는 ID 조회
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<ApiErrorResponse> {
        log.warn(ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse.of(HttpStatus.NOT_FOUND.value(), ex.message ?: "리소스를 찾을 수 없습니다"))
    }

    /**
     * 비즈니스 로직 실패 (422 Unprocessable Entity)
     * - 결제 실패
     * - 상태 검증 실패
     * - 비즈니스 규칙 위반
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ApiErrorResponse> {
        log.warn(ex.message)
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.message ?: "처리할 수 없는 요청입니다"))
    }

    /**
     * 예상치 못한 서버 오류 (500 Internal Server Error)
     * - 시스템 장애
     * - 예상하지 못한 예외
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ApiErrorResponse> {
        log.error(
            "예상치 못한 RuntimeException 발생 - type: {}, message: {}",
            ex.javaClass.simpleName,
            ex.message
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 오류가 발생했습니다"))
    }
}
