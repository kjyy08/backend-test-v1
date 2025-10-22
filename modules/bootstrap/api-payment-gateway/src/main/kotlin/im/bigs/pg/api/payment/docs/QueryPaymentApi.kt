package im.bigs.pg.api.payment.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "결제 내역 조회",
    description = "결제 내역을 조회하고 통계 정보를 함께 제공합니다.",
    parameters = [
        Parameter(
            name = "partnerId",
            description = "파트너 ID",
            `in` = ParameterIn.QUERY,
            required = false,
            example = "2"
        ),
        Parameter(
            name = "status",
            description = "결제 상태 (APPROVED, CANCELED)",
            `in` = ParameterIn.QUERY,
            required = false,
            example = "APPROVED"
        ),
        Parameter(
            name = "from",
            description = "조회 시작 일시 (형식: yyyy-MM-dd HH:mm:ss)",
            `in` = ParameterIn.QUERY,
            required = false,
            example = "2025-01-01 00:00:00"
        ),
        Parameter(
            name = "to",
            description = "조회 종료 일시 (형식: yyyy-MM-dd HH:mm:ss)",
            `in` = ParameterIn.QUERY,
            required = false,
            example = "2025-12-31 23:59:59"
        ),
        Parameter(
            name = "cursor",
            description = "페이지네이션 커서 (이전 응답의 nextCursor 값)",
            `in` = ParameterIn.QUERY,
            required = false
        ),
        Parameter(
            name = "limit",
            description = "한 페이지당 조회 개수 (기본값: 20)",
            `in` = ParameterIn.QUERY,
            required = false,
            example = "20"
        )
    ]
)
annotation class QueryPaymentApi
