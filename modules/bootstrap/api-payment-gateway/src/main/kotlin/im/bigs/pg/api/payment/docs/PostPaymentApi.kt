package im.bigs.pg.api.payment.docs

import im.bigs.pg.api.payment.dto.CreatePaymentRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "결제 생성",
    description = "새로운 결제를 생성하고 외부 PG를 통해 수수료 및 정산금 계산 결과를 저장합니다.",
    requestBody = RequestBody(
        description = "결제 생성 요청",
        required = true,
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = CreatePaymentRequest::class),
                examples = [
                    ExampleObject(
                        name = "Test PG 서버 결제 성공 카드",
                        value = """
                        {
                          "partnerId": 2,
                          "amount": 10000,
                          "cardBin": "111111",
                          "cardLast4": "1111",
                          "productName": "테스트 상품"
                        }
                        """
                    ),
                    ExampleObject(
                        name = "Test PG 서버 결제 실패 카드",
                        value = """
                        {
                          "partnerId": 2,
                          "amount": 10000,
                          "cardBin": "222222",
                          "cardLast4": "2222",
                          "productName": "테스트 상품"
                        }
                        """
                    ),
                ]
            )
        ]
    )
)
annotation class PostPaymentApi
