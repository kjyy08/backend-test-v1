package im.bigs.pg.external.pg.testpg.utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object Aes256GcmEncryptor {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_BIT = 128

    /**
     * 평문을 AES-256-GCM으로 암호화하고 Base64URL로 인코딩합니다.
     *
     * @param plaintext 암호화할 평문 문자열
     * @param apiKey API-KEY (UUID 형식)
     * @param ivBase64Url IV (Base64URL 인코딩된 12바이트)
     * @return Base64URL 인코딩된 암호문 (ciphertext + tag)
     */
    fun encrypt(plaintext: String, apiKey: String, ivBase64Url: String): String {
        // API-KEY를 SHA-256으로 해싱하여 32바이트 키 생성
        val key = generateKey(apiKey)

        // IV를 Base64URL 디코딩 (12바이트)
        val iv = Base64.getUrlDecoder().decode(ivBase64Url)

        require(iv.size == 12) {
            "IV는 12바이트여야 합니다. 현재 IV 크기: ${iv.size}"
        }

        // AES-256-GCM Cipher 초기화
        val cipher = Cipher.getInstance(ALGORITHM)
        val secretKey = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(TAG_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        // 평문을 UTF-8 바이트로 변환 후 암호화
        val plaintextBytes = plaintext.toByteArray(StandardCharsets.UTF_8)
        val ciphertextWithTag = cipher.doFinal(plaintextBytes)

        // 암호문을 Base64URL로 인코딩
        return Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertextWithTag)
    }

    /**
     * API-KEY를 SHA-256으로 해싱하여 32바이트 AES 키를 생성합니다.
     *
     * @param apiKey API-KEY 문자열
     * @return SHA-256 해시 결과 (32 bytes)
     */
    fun generateKey(apiKey: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(apiKey.toByteArray(StandardCharsets.UTF_8))
    }
}
