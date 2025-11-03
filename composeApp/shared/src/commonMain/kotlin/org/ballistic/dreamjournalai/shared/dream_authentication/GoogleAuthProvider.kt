package org.ballistic.dreamjournalai.shared.dream_authentication

expect object GoogleAuthProvider {
    suspend fun provideGoogleAuth(context: Any?): Result
}

sealed class Result {
    data class Success(val account: Account) : Result()
    data class Error(val message: String) : Result()
    object Cancelled : Result()
}

data class Account(
    val idToken: String,
    val accessTokenOrNonce: String
) {
    val isEmpty: Boolean = idToken.isEmpty() || accessTokenOrNonce.isEmpty()
}
