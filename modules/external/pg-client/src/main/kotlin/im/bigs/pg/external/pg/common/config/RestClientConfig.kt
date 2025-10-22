package im.bigs.pg.external.pg.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class RestClientConfig {

    @Bean
    fun restClient(): RestClient {
        val factory = SimpleClientHttpRequestFactory()
        factory.setReadTimeout(Duration.ofSeconds(15))
        factory.setConnectTimeout(Duration.ofSeconds(10))

        return RestClient.builder()
            .requestFactory(factory)
            .build()
    }
}
