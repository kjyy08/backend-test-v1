package im.bigs.pg.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(
        @Value("\${springdoc.server.url}") serverUrl: String,
        @Value("\${springdoc.server.description}") serverDescription: String
    ): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("백엔드 사전 과제 - 결제 도메인 서버")
                    .description(
                        """
            나노바나나 페이먼츠 백엔드 사전 과제
            
            ## 주요 API
            - 결제 생성 (POST /api/v1/payments)
            - 결제 내역 조회 (GET /api/v1/payments)
            
            ## 추가 구현한 선택 과제
            - 오픈 API 문서화 및 간단한 운영 지표 추가
                        """.trimIndent()
                    )
                    .contact(
                        Contact()
                            .name("김주엽")
                            .url("https://github.com/kjyy08")
                            .email("kjyy08@naver.com")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url(serverUrl)
                        .description(serverDescription)
                )
            )
    }
}
