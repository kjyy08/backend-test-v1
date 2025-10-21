package im.bigs.pg.external.pg.testpg.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "pg.testpg")
data class TestPgConfig(
    var apiKey: String = "11111111-1111-4111-8111-111111111111",
    var baseUrl: String = "https://api-test-pg.bigs.im",
    var ivBase64Url: String = "AAAAAAAAAAAAAAAA"
)
