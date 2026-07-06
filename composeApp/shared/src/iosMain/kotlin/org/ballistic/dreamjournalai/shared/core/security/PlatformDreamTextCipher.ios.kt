package org.ballistic.dreamjournalai.shared.core.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreCrypto.CC_SHA512
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCHmacAlgSHA256
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCSuccess
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual class PlatformDreamTextCipher actual constructor() : DreamTextCipher {
    @OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
    actual override fun encrypt(plainText: String, userId: String): String {
        if (plainText.isBlank() || plainText.startsWith(DREAM_TEXT_CIPHER_PREFIX) || userId.isBlank()) {
            return plainText
        }

        val keyMaterial = keyMaterial(userId)
        val aesKey = keyMaterial.copyOfRange(0, 32)
        val macKey = keyMaterial.copyOfRange(32, 64)
        val iv = secureRandomBytes(IV_SIZE)
        val ciphertext = aesCbcCrypt(
            operation = kCCEncrypt,
            key = aesKey,
            iv = iv,
            input = plainText.encodeToByteArray()
        )
        val authenticatedData = byteArrayOf(VERSION) + iv + ciphertext
        val tag = hmacSha256(macKey, authenticatedData)

        return DREAM_TEXT_CIPHER_PREFIX + Base64.encode(authenticatedData + tag)
    }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
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
            require(storedTag.constantTimeEquals(expectedTag))

            val iv = authenticatedData.copyOfRange(VERSION_SIZE, VERSION_SIZE + IV_SIZE)
            val ciphertext = authenticatedData.copyOfRange(VERSION_SIZE + IV_SIZE, authenticatedData.size)
            aesCbcCrypt(
                operation = kCCDecrypt,
                key = aesKey,
                iv = iv,
                input = ciphertext
            ).decodeToString()
        }.getOrElse { storedText }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keyMaterial(userId: String): ByteArray {
        val input = "$DREAM_TEXT_CIPHER_CONTEXT:$userId".encodeToByteArray()
        val digest = ByteArray(SHA512_SIZE)
        input.usePinned { inputPinned ->
            digest.usePinned { digestPinned ->
                CC_SHA512(
                    inputPinned.addressOf(0),
                    input.size.convert(),
                    digestPinned.addressOf(0).reinterpret()
                )
            }
        }
        return digest
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun secureRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        val status = bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, size.convert(), pinned.addressOf(0))
        }
        check(status == errSecSuccess) { "Unable to create encryption IV." }
        return bytes
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun aesCbcCrypt(
        operation: UInt,
        key: ByteArray,
        iv: ByteArray,
        input: ByteArray
    ): ByteArray = memScoped {
        val output = ByteArray(input.size + AES_BLOCK_SIZE)
        val outputLength = alloc<ULongVar>()
        val status = key.usePinned { keyPinned ->
            iv.usePinned { ivPinned ->
                input.usePinned { inputPinned ->
                    output.usePinned { outputPinned ->
                        CCCrypt(
                            operation,
                            kCCAlgorithmAES,
                            kCCOptionPKCS7Padding,
                            keyPinned.addressOf(0),
                            key.size.convert(),
                            ivPinned.addressOf(0),
                            inputPinned.addressOf(0),
                            input.size.convert(),
                            outputPinned.addressOf(0),
                            output.size.convert(),
                            outputLength.ptr
                        )
                    }
                }
            }
        }
        check(status == kCCSuccess) { "Dream text encryption failed." }
        output.copyOf(outputLength.value.toInt())
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val output = ByteArray(HMAC_SHA256_SIZE)
        key.usePinned { keyPinned ->
            data.usePinned { dataPinned ->
                output.usePinned { outputPinned ->
                    CCHmac(
                        kCCHmacAlgSHA256,
                        keyPinned.addressOf(0),
                        key.size.convert(),
                        dataPinned.addressOf(0),
                        data.size.convert(),
                        outputPinned.addressOf(0)
                    )
                }
            }
        }
        return output
    }

    private fun ByteArray.constantTimeEquals(other: ByteArray): Boolean {
        if (size != other.size) return false
        var diff = 0
        for (index in indices) {
            diff = diff or (this[index].toInt() xor other[index].toInt())
        }
        return diff == 0
    }

    private companion object {
        const val VERSION: Byte = 1
        const val VERSION_SIZE = 1
        const val IV_SIZE = 16
        const val TAG_SIZE = 32
        const val AES_BLOCK_SIZE = 16
        const val SHA512_SIZE = 64
        const val HMAC_SHA256_SIZE = 32
    }
}
