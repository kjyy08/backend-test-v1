package im.bigs.pg.application.payment.port.out

/**
 * 결제 메트릭 수집 포트.
 * - 결제 금액, 수수료 등 도메인 특화 메트릭을 기록합니다.
 */
interface PaymentMetricsOutPort {

    /**
     * 값의 분포를 기록합니다 (결제 금액, 수수료, 순수익 등).
     *
     * @param name 메트릭 이름 (예: "payment.amount")
     * @param value 기록할 값
     * @param tags 태그 맵 (예: mapOf("partnerId" to "1"))
     */
    fun recordValue(name: String, value: Double, tags: Map<String, String> = emptyMap())
}
