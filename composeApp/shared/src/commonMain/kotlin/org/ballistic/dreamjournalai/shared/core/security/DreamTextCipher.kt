package org.ballistic.dreamjournalai.shared.core.security

interface DreamTextCipher {
    fun encrypt(plainText: String, userId: String): String
    fun decrypt(storedText: String, userId: String): String
}

expect class PlatformDreamTextCipher() : DreamTextCipher {
    override fun encrypt(plainText: String, userId: String): String
    override fun decrypt(storedText: String, userId: String): String
}

internal const val DREAM_TEXT_CIPHER_PREFIX = "dnenc1:"
internal const val DREAM_TEXT_CIPHER_CONTEXT = "DreamNorth:user-dream-text:v1"
