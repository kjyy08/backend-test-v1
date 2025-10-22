package im.bigs.pg.external.pg.testpg.utils

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertNotEquals

class Aes256GcmEncryptorTest {

    @Test
    fun `API KEY는 SHA-256으로 해싱되어 32바이트 키로 생성되어야 한다`() {
        // given
        val apiKey = "11111111-1111-4111-8111-111111111111"
        val expectedHash = "bd7662a5eeb41614e720d477abfcb2272e19a8a70a93b7e3bc8560d44ad326e9"
        val expectedBytes = expectedHash.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        // when
        val key = Aes256GcmEncryptor.generateKey(apiKey)

        // then
        assertEquals(32, key.size)
        assertArrayEquals(expectedBytes, key)
    }

    @Test
    fun `정상적인 입력인 경우 암호화에 성공한다`() {
        // given
        val plaintext = """
            {
                "cardNumber":"1111-1111-1111-1111",
                "birthDate":"19900101",
                "expiry":"1227",
                "password":"12",
                "amount":10000
            }
        """.trimIndent()
        val apiKey = "11111111-1111-4111-8111-111111111111"
        val ivBase64Url = "AAAAAAAAAAAAAAAA"
        val expectedEnc =
            "FnKTvMMGb8OO_HMYaGXF8j_83wr0n7zodWtWRo6X6-ccrzMZgFDeUMT9b8ZkzxMqqwg9DFrjgjRSBc2cv8jxVUUmrGcWLLDZixaPHGT4qPFazcHhqhaWdez7LzBCs5TX5w835163wLi3m744PSFkve7d2HWNeLxf8Su6cn4znqiTxV2b30F2JIO9IHU7EtXl62Mt3g"

        // when
        val enc = Aes256GcmEncryptor.encrypt(plaintext, apiKey, ivBase64Url)

        // then
        assertNotNull(enc)
        assertEquals(enc, expectedEnc)
        assertFalse(enc.contains("="))
        assertFalse(enc.contains("+"))
        assertFalse(enc.contains("/"))
    }

    @Test
    fun `동일한 입력에 대해 동일한 암호문을 생성한다`() {
        // given
        val plaintext = "test message"
        val apiKey = "test-api-key"
        val ivBase64Url = "AAAAAAAAAAAAAAAA"

        // when
        val result1 = Aes256GcmEncryptor.encrypt(plaintext, apiKey, ivBase64Url)
        val result2 = Aes256GcmEncryptor.encrypt(plaintext, apiKey, ivBase64Url)

        // then
        assertEquals(result1, result2)
    }

    @Test
    fun `IV가 12바이트가 아니면 예외가 발생한다`() {
        // given
        val plaintext = "test"
        val apiKey = "test-api-key"
        val invalidIv = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(16))

        // when & then
        assertThatThrownBy { Aes256GcmEncryptor.encrypt(plaintext, apiKey, invalidIv) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `API KEY가 달라지면 암호문이 다르게 생성되어야 한다`() {
        // given
        val plaintext = "same plaintext"
        val apiKey1 = "key-1"
        val apiKey2 = "key-2"
        val ivBase64Url = "AAAAAAAAAAAAAAAA"

        // when
        val result1 = Aes256GcmEncryptor.encrypt(plaintext, apiKey1, ivBase64Url)
        val result2 = Aes256GcmEncryptor.encrypt(plaintext, apiKey2, ivBase64Url)

        // then
        assertNotEquals(result1, result2)
    }
}
