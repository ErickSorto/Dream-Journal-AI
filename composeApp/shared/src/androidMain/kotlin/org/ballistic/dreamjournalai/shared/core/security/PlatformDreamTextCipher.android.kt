package org.ballistic.dreamjournalai.shared.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual class PlatformDreamTextCipher actual constructor() : DreamTextCipher {
    private val secureRandom = SecureRandom()

    @OptIn(ExperimentalEncodingApi::class)
    actual override fun encrypt(plainText: String, userId: String): String {
        if (plainText.isBlank() || plainText.startsWith(DREAM_TEXT_CIPHER_PREFIX) || userId.isBlank()) {
            return plainText
        }

        val keyMaterial = keyMaterial(userId)
        val aesKey = keyMaterial.copyOfRange(0, 32)
        val macKey = keyMaterial.copyOfRange(32, 64)
        val iv = ByteArray(16).also(secureRandom::nextBytes)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(aesKey, "AES"), IvParameterSpec(iv))
        val ciphertext = cipher.doFinal(plainText.encodeToByteArray())
        val authenticatedData = byteArrayOf(VERSION) + iv + ciphertext
        val tag = hmacSha256(macKey, authenticatedData)

        return DREAM_TEXT_CIPHER_PREFIX + Base64.encode(authenticatedData + tag)
    }

    @OptIn(ExperimentalEncodingApi::class)
    actual override fun decrypt(storedText: String, userId: String): String {
        if (!storedText.startsWith(DREAM_TEXT_CIPHER_PREFIX) || userId.isBlank()) {
            return storedText
        }

        return runCatching {
            val payload = Base64.decode(storedText.removePrefix(DREAM_TEXT_CIPHER_PREFIX))
            require(payload.size > VERSION_SIZE + IV_SIZE + TAG_SIZE)
            require(payload[0] == VERSION)

            val authenticatedData = payload.copyOfRange(0, payload.size - TAG_SIZE)
            val storedTag = payload.copyOfRange(payload.size - TAG_SIZE, payload.size)
            val keyMaterial = keyMaterial(userId)
            val aesKey = keyMaterial.copyOfRange(0, 32)
            val macKey = keyMaterial.copyOfRange(32, 64)
            val expectedTag = hmacSha256(macKey, authenticatedData)
            require(MessageDigest.isEqual(storedTag, expectedTag))

            val iv = authenticatedData.copyOfRange(VERSION_SIZE, VERSION_SIZE + IV_SIZE)
            val ciphertext = authenticatedData.copyOfRange(VERSION_SIZE + IV_SIZE, authenticatedData.size)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(aesKey, "AES"), IvParameterSpec(iv))
            cipher.doFinal(ciphertext).decodeToString()
        }.getOrElse { storedText }
    }

    private fun keyMaterial(userId: String): ByteArray {
        val input = "$DREAM_TEXT_CIPHER_CONTEXT:$userId".encodeToByteArray()
        return MessageDigest.getInstance("SHA-512").digest(input)
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    private companion object {
        const val VERSION: Byte = 1
        const val VERSION_SIZE = 1
        const val IV_SIZE = 16
        const val TAG_SIZE = 32
    }
}
