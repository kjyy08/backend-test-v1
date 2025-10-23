package im.bigs.pg.infra.metrics.payment

import im.bigs.pg.application.payment.port.out.PaymentMetricsOutPort
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.springframework.stereotype.Component

@Component
class PaymentRegistry(
    private val meterRegistry: MeterRegistry
) : PaymentMetricsOutPort {

    override fun recordValue(name: String, value: Double, tags: Map<String, String>) =
        meterRegistry.summary(name, tags.toTags())
            .record(value)

    private fun Map<String, String>.toTags(): List<Tag> = map { Tag.of(it.key, it.value) }
}
