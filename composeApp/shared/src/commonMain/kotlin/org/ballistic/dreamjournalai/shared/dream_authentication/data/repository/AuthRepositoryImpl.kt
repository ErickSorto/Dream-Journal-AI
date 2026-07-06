package org.ballistic.dreamjournalai.shared.dream_authentication.data.repository

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.AuthResult
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FieldValue.Companion.serverTimestamp
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.functions.FirebaseFunctionsException
import dev.gitlive.firebase.functions.code
import dev.gitlive.firebase.functions.details
import dev.gitlive.firebase.functions.functions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.AuthRepository
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.DailyDreamTokenClaim
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.AuthStateResponse
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.ReloadUserResponse
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.SendPasswordResetEmailResponse
import org.ballistic.dreamjournalai.shared.core.Constants.CREATED_AT
import org.ballistic.dreamjournalai.shared.core.Constants.DISPLAY_NAME
import org.ballistic.dreamjournalai.shared.core.Constants.EMAIL
import org.ballistic.dreamjournalai.shared.core.Constants.USERS
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.dream_premium.domain.repository.PremiumPaywallRepository
import kotlin.time.ExperimentalTime

class AuthRepositoryImpl (
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val premiumPaywallRepository: PremiumPaywallRepository,
) : AuthRepository {
    private var anonymousUserId: String? = null

    private val _dreamTokens = MutableStateFlow(0)
    override val dreamTokens: StateFlow<Int> = _dreamTokens

    private val _dailyTokenStreak = MutableStateFlow(0)
    override val dailyTokenStreak: StateFlow<Int> = _dailyTokenStreak

    private val _dailyTokenCompletedWeeks = MutableStateFlow(0)
    override val dailyTokenCompletedWeeks: StateFlow<Int> = _dailyTokenCompletedWeeks

    private val _hasClaimedDailyToken = MutableStateFlow(false)
    override val hasClaimedDailyToken: StateFlow<Boolean> = _hasClaimedDailyToken

    private val _dailyTokensClaimedToday = MutableStateFlow(0)
    override val dailyTokensClaimedToday: StateFlow<Int> = _dailyTokensClaimedToday

    private val _lastDailyTokenClaimDay = MutableStateFlow<String?>(null)
    override val lastDailyTokenClaimDay: StateFlow<String?> = _lastDailyTokenClaimDay

    private val _hasGeneratedDreamWorld = MutableStateFlow(false)
    override val hasGeneratedDreamWorld: StateFlow<Boolean> = _hasGeneratedDreamWorld

    private val _isUserExist = MutableStateFlow(false)
    override val isUserExist: StateFlow<Boolean> = _isUserExist

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _emailVerified = MutableStateFlow(false)
    override val isEmailVerified: StateFlow<Boolean> = _emailVerified

    private val _isUserAnonymous = MutableStateFlow(false)
    override val isUserAnonymous: StateFlow<Boolean> = _isUserAnonymous

    init {
        // Initialize with current state
        val currentUser = auth.currentUser
        _isUserAnonymous.value = currentUser?.isAnonymous == true
        
        // Listen for auth changes to keep isUserAnonymous updated
        CoroutineScope(Dispatchers.Main).launch {
            auth.authStateChanged.collect { user ->
                _isUserAnonymous.value = user?.isAnonymous == true
                validateUser() // Re-validate other flags
                premiumPaywallRepository.syncAppUser(user?.uid)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun firebaseSignInWithGoogle(
        googleCredential: AuthCredential
    ): Flow<Resource<Pair<AuthResult, String?>>> {
        return firebaseSignInWithCredential(
            credential = googleCredential,
            providerName = "Google"
        )
    }

    override suspend fun firebaseSignInWithApple(
        appleCredential: AuthCredential
    ): Flow<Resource<Pair<AuthResult, String?>>> {
        return firebaseSignInWithCredential(
            credential = appleCredential,
            providerName = "Apple"
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun firebaseSignInWithCredential(
        credential: AuthCredential,
        providerName: String
    ): Flow<Resource<Pair<AuthResult, String?>>> {
        return flow {
            Logger.d { "AuthRepositoryImpl: firebaseSignInWith$providerName called with credential=$credential" }
             emit(Resource.Loading())
             try {
                 val anonymousUserId = if (auth.currentUser?.isAnonymous == true) {
                     auth.currentUser?.uid
                 } else {
                     null
                 }

                 val result = auth.signInWithCredential(credential)

                Logger.d { "AuthRepositoryImpl: $providerName signInWithCredential succeeded user=${result.user?.uid}, isNew=${result.additionalUserInfo?.isNewUser}" }
                 val isNewUser = result.additionalUserInfo?.isNewUser == true

                // For brand-new federated users, create the Firestore doc with safe defaults.
                if (isNewUser) {
                    addUserToFirestore(registrationTimestamp = kotlin.time.Clock.System.now().toEpochMilliseconds())
                }

                 emit(Resource.Success(Pair(result, anonymousUserId)))
             } catch (e: FirebaseAuthUserCollisionException) {
                Logger.e("AuthRepositoryImpl") { "FirebaseAuthUserCollisionException: ${e.message}" }
                 emit(Resource.Error("An existing user account was found with the same credentials: ${e.message}"))
             } catch (e: Exception) {
                Logger.e("AuthRepositoryImpl") { "firebaseSignInWith$providerName failed: ${e.message}" }
                 emit(Resource.Error("Failed to sign in with $providerName: ${e.message}"))
             }

         }
     }

    override suspend fun consumeDreamTokens(tokensToConsume: Int): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (tokensToConsume <= 0) return@withContext Resource.Success(true)
                Firebase.auth.currentUser
                    ?: return@withContext Resource.Error("User is not logged in.")

                val result = Firebase.functions
                    .httpsCallable("spendDreamTokens")
                    .invoke(
                        mapOf(
                            "tokensToSpend" to tokensToConsume,
                            "reason" to "client_action"
                        )
                    )
                val response = result.data<SpendDreamTokensResponse>()
                _dreamTokens.value = response.totalTokens
                Resource.Success(response.success)
            } catch (e: FirebaseFunctionsException) {
                val message = when (e.code.name.lowercase()) {
                    "failed-precondition" -> "Not enough dream tokens available."
                    "unauthenticated" -> "User is not logged in."
                    else -> e.message ?: "Failed to use dream tokens."
                }
                Resource.Error(message)
            } catch (e: Exception) {
                Resource.Error("Failed to consume dream tokens: ${e.message}")
            }
        }
    }

    @Serializable
    private data class SpendDreamTokensResponse(
        val success: Boolean = false,
        val tokensSpent: Int = 0,
        val totalTokens: Int = 0,
    )

    @Serializable
    private data class ClaimDailyDreamTokensResponse(
        val tokensAwarded: Int = 0,
        val totalTokens: Int = 0,
        val streak: Int = 0,
        val claimDay: String = "",
        val tokensAwardedToday: Int = 0,
        val dailyTokenAllowance: Int = 1,
        val bonusTokensAwarded: Int = 0,
        val completedWeeks: Int = 0,
    )

    override suspend fun claimDailyDreamTokens(): Resource<DailyDreamTokenClaim> {
        return withContext(Dispatchers.IO) {
            try {
                val user = Firebase.auth.currentUser
                    ?: return@withContext Resource.Error("User is not logged in.")
                if (user.isAnonymous) {
                    return@withContext Resource.Error("Must sign in.")
                }

                val result = Firebase.functions
                    .httpsCallable("claimDailyDreamTokens")
                    .invoke()

                val response = result.data<ClaimDailyDreamTokensResponse>()
                _dreamTokens.value = response.totalTokens
                _dailyTokenStreak.value = response.streak
                _dailyTokenCompletedWeeks.value = response.completedWeeks
                _dailyTokensClaimedToday.value = response.tokensAwardedToday
                _hasClaimedDailyToken.value = response.tokensAwardedToday >= response.dailyTokenAllowance
                _lastDailyTokenClaimDay.value = response.claimDay.takeIf { it.isNotBlank() }
                Resource.Success(
                    DailyDreamTokenClaim(
                        tokensAwarded = response.tokensAwarded,
                        totalTokens = response.totalTokens,
                        streak = response.streak,
                        claimDay = response.claimDay,
                        tokensAwardedToday = response.tokensAwardedToday,
                        dailyTokenAllowance = response.dailyTokenAllowance,
                        bonusTokensAwarded = response.bonusTokensAwarded,
                        completedWeeks = response.completedWeeks
                    )
                )
            } catch (e: FirebaseFunctionsException) {
                val normalizedCode = e.code.name.lowercase()
                if (normalizedCode == "not-found" || normalizedCode == "not_found") {
                    Logger.w("AuthRepositoryImpl") {
                        "claimDailyDreamTokens callable was not found; falling back to Firestore claim."
                    }
                    return@withContext claimDailyDreamTokensFromFirestore()
                }

                val message = when (normalizedCode) {
                    "failed-precondition" -> "Daily token already claimed."
                    "unauthenticated" -> "User is not logged in."
                    else -> e.message ?: "Failed to claim daily token."
                }
                Resource.Error(message)
            } catch (e: Exception) {
                Resource.Error("Failed to claim daily token: ${e.message}")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun claimDailyDreamTokensFromFirestore(): Resource<DailyDreamTokenClaim> {
        return try {
            val user = Firebase.auth.currentUser
                ?: return Resource.Error("User is not logged in.")
            if (user.isAnonymous) {
                return Resource.Error("Must sign in.")
            }

            val todayDate = kotlin.time.Clock.System.todayIn(TimeZone.UTC)
            val today = todayDate.toString()
            val yesterday = todayDate.minus(DatePeriod(days = 1)).toString()
            val userDocRef = Firebase.firestore.collection(USERS).document(user.uid)
            val snapshot = userDocRef.get()
            val lastClaimDay = snapshot.getOptionalString("lastDailyDreamTokenClaimDay")
                ?: snapshot.getOptionalString("lastDailyDreamTokenClaimDate")
            val tokensAlreadyAwardedToday = if (lastClaimDay == today) {
                snapshot.getOptionalInt("lastDailyDreamTokensAwarded")
                    ?: snapshot.getOptionalLong("lastDailyDreamTokensAwarded")?.toInt()
                    ?: 1
            } else {
                0
            }

            if (tokensAlreadyAwardedToday >= DAILY_TOKEN_FREE_AWARD) {
                _dailyTokensClaimedToday.value = tokensAlreadyAwardedToday
                _hasClaimedDailyToken.value = true
                return Resource.Error("Daily token already claimed.")
            }

            val tokensAwarded = DAILY_TOKEN_FREE_AWARD - tokensAlreadyAwardedToday
            val currentTokens = snapshot.getDreamTokenBalance()
            val previousStreak = snapshot.getOptionalInt("dailyDreamTokenStreak")
                ?: snapshot.getOptionalLong("dailyDreamTokenStreak")?.toInt()
                ?: 0
            val newStreak = when (lastClaimDay) {
                today -> previousStreak
                yesterday -> previousStreak + 1
                else -> 1
            }
            val previousCompletedWeeks = maxOf(
                snapshot.getDailyTokenCompletedWeeks(),
                previousStreak / DAILY_TOKEN_STREAK_BONUS_INTERVAL
            )
            val completedWeeks = maxOf(
                previousCompletedWeeks,
                newStreak / DAILY_TOKEN_STREAK_BONUS_INTERVAL
            )
            val bonusAlreadyAwardedToday = snapshot.getOptionalString("lastDailyDreamTokenBonusDay") == today
            val bonusTokensAwarded = if (
                lastClaimDay != today &&
                newStreak > 0 &&
                newStreak % DAILY_TOKEN_STREAK_BONUS_INTERVAL == 0 &&
                !bonusAlreadyAwardedToday
            ) {
                DAILY_TOKEN_STREAK_BONUS_AWARD
            } else {
                0
            }
            val tokensAwardedToday = tokensAlreadyAwardedToday + tokensAwarded
            val totalAwarded = tokensAwarded + bonusTokensAwarded
            val totalTokens = currentTokens + totalAwarded
            val bonusFields = if (bonusTokensAwarded > 0) {
                mapOf(
                    "lastDailyDreamTokenBonusDay" to today,
                    "lastDailyDreamTokenBonusAwardedAt" to serverTimestamp,
                    "lastDailyDreamTokenBonusAward" to bonusTokensAwarded.toLong(),
                )
            } else {
                emptyMap()
            }
            val claimFields = mapOf(
                "dreamTokens" to totalTokens.toLong(),
                "lastDailyDreamTokenClaimDay" to today,
                "lastDailyDreamTokenClaimDate" to today,
                "lastDailyDreamTokenClaimedAt" to serverTimestamp,
                "lastDailyDreamTokensAwarded" to tokensAwardedToday.toLong(),
                "lastDailyDreamTokenAllowance" to DAILY_TOKEN_FREE_AWARD.toLong(),
                "dailyDreamTokenStreak" to newStreak.toLong(),
                "dailyDreamTokenCompletedWeeks" to completedWeeks.toLong(),
            ) + bonusFields

            if (snapshot.exists) {
                userDocRef.update(claimFields)
            } else {
                userDocRef.set(
                    mapOf(
                        "uid" to user.uid,
                        DISPLAY_NAME to (user.displayName ?: "Dreamer"),
                        EMAIL to (user.email ?: ""),
                        "emailVerified" to (user.isEmailVerified == true),
                        "registrationTimestamp" to serverTimestamp,
                        "lastActiveTimestamp" to serverTimestamp,
                        "unlockedWords" to emptyList<String>(),
                        "hasGeneratedDreamWorld" to false,
                        "hasCompletedOnboarding" to false,
                    ) + claimFields
                )
            }

            _dreamTokens.value = totalTokens
            _dailyTokenStreak.value = newStreak
            _dailyTokenCompletedWeeks.value = completedWeeks
            _dailyTokensClaimedToday.value = tokensAwardedToday
            _hasClaimedDailyToken.value = tokensAwardedToday >= DAILY_TOKEN_FREE_AWARD
            _lastDailyTokenClaimDay.value = today
            Resource.Success(
                DailyDreamTokenClaim(
                    tokensAwarded = totalAwarded,
                    totalTokens = totalTokens,
                    streak = newStreak,
                    claimDay = today,
                    tokensAwardedToday = tokensAwardedToday,
                    dailyTokenAllowance = DAILY_TOKEN_FREE_AWARD,
                    bonusTokensAwarded = bonusTokensAwarded,
                    completedWeeks = completedWeeks
                )
            )
        } catch (e: Exception) {
            Logger.e("AuthRepositoryImpl") { "Firestore daily token fallback failed: ${e.message}" }
            Resource.Error("Failed to claim daily token: ${e.message}")
        }
    }


    override suspend fun unlockWord(word: String, tokenCost: Int): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Firebase.auth.currentUser
                    ?: return@withContext Resource.Error("User is not logged in.")

                val result = Firebase.functions
                    .httpsCallable("unlockDreamSymbol")
                    .invoke(
                        mapOf(
                            "word" to word,
                            "tokenCost" to tokenCost
                        )
                    )
                val response = result.data<UnlockDreamSymbolResponse>()
                _dreamTokens.value = response.totalTokens
                Resource.Success(response.success)
            } catch (e: FirebaseFunctionsException) {
                val message = when (e.code.name.lowercase()) {
                    "failed-precondition" -> "Not enough dream tokens"
                    "unauthenticated" -> "User is not logged in."
                    else -> e.message ?: "Failed to unlock word."
                }
                Resource.Error(message)
            } catch (e: Exception) {
                Resource.Error("Failed to unlock word: ${e.message}")
            }
        }
    }

    @Serializable
    private data class UnlockDreamSymbolResponse(
        val success: Boolean = false,
        val word: String = "",
        val alreadyUnlocked: Boolean = false,
        val tokensSpent: Int = 0,
        val totalTokens: Int = 0,
        val unlockedWords: List<String> = emptyList(),
    )

    override suspend fun getUnlockedWords(): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())

        // Retrieve the current user
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            emit(Resource.Error("No user is logged in."))
            return@flow
        }

        try {
            // Reference to the user's document: USERS/{uid}
            val userDocRef = Firebase.firestore
                .collection(USERS)
                .document(currentUser.uid)

            // Fetch the user document (suspend call)
            val snapshot = userDocRef.get()

            // Check if the document exists
            if (snapshot.exists) {
                val unlockedWords = snapshot.getStringList("unlockedWords")

                Logger.d("Unlocked words: $unlockedWords")
                emit(Resource.Success(unlockedWords))
            } else {
                emit(Resource.Error("User document does not exist."))
            }
        } catch (e: Exception) {
            Logger.e("Failed to get unlocked words", e)
            emit(Resource.Error("Failed to get unlocked words: ${e.message}"))
        }
    }


    override suspend fun recordUserInteraction() {
        // If you need to reload the user, do it via dev.gitlive as well
        reloadFirebaseUser()

        val user = Firebase.auth.currentUser
        if (user != null) {
            try {
                val userDocRef = Firebase.firestore.collection(USERS).document(user.uid)

                // Set the 'lastActiveTimestamp' to server time, merging with existing fields
                userDocRef.set(
                    data = mapOf("lastActiveTimestamp" to FieldValue.serverTimestamp),
                    merge = true
                )

            } catch (e: Exception) {
                Logger.e("AuthRepositoryImpl"){ "Failed to record user interaction: $e" }
            }
        }
    }


    private suspend fun validateUser() {
        val user = Firebase.auth.currentUser

        // Update your local states or flows
        _isUserExist.value = user != null
        _emailVerified.value = user?.isEmailVerified ?: false
        _isLoggedIn.value = user != null && user.isEmailVerified == true
        _isUserAnonymous.value = user?.isAnonymous ?: false

        // If user is verified, update FireStore
        if (user != null && user.isEmailVerified) {
            try {
                val userDocRef = Firebase
                    .firestore
                    .collection(USERS)
                    .document(user.uid)

                // 'update(...)' is a suspend function in dev.gitlive
                userDocRef.update(mapOf("emailVerified" to true))

                // Optionally log success
                // Log.d("Firestore", "Email verification status updated successfully")
            } catch (e: Exception) {
                // Log or handle the error
                // Log.e("Firestore", "Error updating email verification status: ", e)
            }
        }
    }

    override suspend fun anonymousSignIn(): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val authResult = Firebase.auth.signInAnonymously()
            emit(Resource.Success(authResult))
        }.catch { e ->
            emit(Resource.Error(e.toString()))
        }
    }
    private suspend fun addUserToFirestore(registrationTimestamp: Long) {
        auth.currentUser?.apply {
            val docRef = db.collection(USERS).document(uid)
            val snapshot = try { docRef.get() } catch (_: Exception) { null }

            val hasDoc = snapshot?.exists == true
            val hasTokens = snapshot?.hasDreamTokenField() == true

            if (!hasDoc) {
                // Create brand-new user doc with safe defaults (includes starting tokens)
                val user = toUser(registrationTimestamp)
                docRef.set(user)
            } else if (!hasTokens) {
                // Doc exists but tokens are truly missing: set only the token field.
                // Do not reset unlockedWords here; existing users may already own symbols.
                docRef.update(
                    mapOf(
                        "dreamTokens" to 25L
                    )
                )
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun firebaseSignUpWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<Resource<String>> {

        // Check if the current user is anonymous and store the user ID if so
        anonymousUserId = if (Firebase.auth.currentUser?.isAnonymous == true) {
            Firebase.auth.currentUser?.uid
        } else {
            null
        }

        @Serializable
        data class CreateAccountResponse(val message: String)

        return flow {
            emit(Resource.Loading())

            try {
                val data = mapOf(
                    "email" to email,
                    "password" to password
                )

                val result = Firebase.functions
                    .httpsCallable("createAccountAndSendEmailVerification")
                    .invoke(data)

                val response = result.data<CreateAccountResponse>()
                val message = response.message
                auth.signInWithEmailAndPassword(email, password)

                emit(Resource.Success(message))

            } catch (e: Exception) {
                emit(Resource.Error("SignUp error: ${e.message}"))
            }
        }.catch { e ->
            emit(Resource.Error(e.toString()))
        }
    }



    override suspend fun sendEmailVerification(): Flow<Resource<Boolean>> {
        return flow {
            emit(Resource.Loading())
            try {
                val currentEmail = auth.currentUser?.email
                    ?: throw IllegalStateException("No email is available for the current user.")
                Firebase.functions
                    .httpsCallable("createAccountAndSendEmailVerification")
                    .invoke(
                        mapOf(
                            "email" to currentEmail,
                            "password" to ""
                        )
                    )
                emit(Resource.Success(true))
            } catch (e: Exception) {
                emit(Resource.Error(e.toString()))
            }
        }
    }


    override suspend fun firebaseSignInWithEmailAndPassword(
        email: String,
        password: String,
    ): Flow<Resource<AuthResult>> {
        anonymousUserId = if (auth.currentUser?.isAnonymous == true) {
            auth.currentUser?.uid
        } else {
            null
        }
        return flow {
            emit(Resource.Loading())
            try {
                val currentUser = auth.currentUser

                if (currentUser != null && anonymousUserId != null && anonymousUserId != "") {
                    val result = auth.signInWithEmailAndPassword(email, password)
                    if (result.user?.isEmailVerified != true) {
                        auth.signOut()
                        emit(Resource.Error("Email not verified. Verify your account, then log in."))
                        return@flow
                    }

                    result.user?.let {
                        transferDreamsFromAnonymousToPermanent(
                            it.uid, anonymousUserId ?: ""
                        )
                    }
                    addUserToFirestore(registrationTimestamp = kotlin.time.Clock.System.now().toEpochMilliseconds())
                    emit(Resource.Success(result))
                } else {
                    val result = auth.signInWithEmailAndPassword(email, password)
                    if (result.user?.isEmailVerified != true) {
                        auth.signOut()
                        emit(Resource.Error("Email not verified. Verify your account, then log in."))
                        return@flow
                    }
                    addUserToFirestore(registrationTimestamp = kotlin.time.Clock.System.now().toEpochMilliseconds())
                    emit(Resource.Success(result))
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                 val result = auth.signInWithEmailAndPassword(email, password)
                if (result.user?.isEmailVerified != true) {
                    auth.signOut()
                    emit(Resource.Error("Email not verified. Verify your account, then log in."))
                    return@flow
                }
                addUserToFirestore(registrationTimestamp = kotlin.time.Clock.System.now().toEpochMilliseconds())
                 emit(Resource.Success(result))
             } catch (e: Exception) {
                 emit(Resource.Error(e.toString()))
             }
            validateUser()
        }
    }


    @OptIn(ExperimentalTime::class)
    override suspend fun reloadFirebaseUser(): ReloadUserResponse {
        return try {
            auth.currentUser?.reload()
            val user = auth.currentUser
            if (user?.isEmailVerified == true) {
                if (!anonymousUserId.isNullOrBlank()) {
                    transferDreamsFromAnonymousToPermanent(user.uid, anonymousUserId ?: "")
                    anonymousUserId = null
                }
                addUserToFirestore(registrationTimestamp = kotlin.time.Clock.System.now().toEpochMilliseconds())
            }
            validateUser()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): SendPasswordResetEmailResponse {
        return try {
            auth.sendPasswordResetEmail(email)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
        premiumPaywallRepository.syncAppUser(null)
    }

    override suspend fun revokeAccess(
        password: String?
    ): Flow<Resource<Boolean>> {
        return flow {
            emit(Resource.Loading())

            try {
                val user = Firebase.auth.currentUser
                    ?: throw IllegalStateException("No authenticated user found")

                Logger.withTag("AuthRepo").d { "revokeAccess start uid=${user.uid} email=${user.email} providerData=${user.providerData.map { it.providerId }} passwordProvided=${!password.isNullOrBlank()}" }

                if (!password.isNullOrBlank() && !user.email.isNullOrBlank()) {
                    val credential = EmailAuthProvider.credential(user.email!!, password)
                    Logger.withTag("AuthRepo").d { "reauthenticating user=${user.uid} via EmailAuthProvider" }
                    user.reauthenticate(credential)
                    Logger.withTag("AuthRepo").d { "reauthenticate success" }
                } else {
                    Logger.withTag("AuthRepo").d { "skipping reauth (no password provided or missing email)" }
                }

                user.delete()
                premiumPaywallRepository.syncAppUser(null)
                Logger.withTag("AuthRepo").d { "user.delete() success" }

                emit(Resource.Success(true))

            } catch (e: Exception) {
                Logger.withTag("AuthRepo").e { "revokeAccess error: ${e.message}" }
                emit(Resource.Error("Failed to revoke access: ${e.message}", false))
            }
        }
    }


    override fun getAuthState(viewModelScope: CoroutineScope): AuthStateResponse {
        return Firebase.auth.authStateChanged
            // Map the Flow<FirebaseUser?> to Flow<Boolean>
            .map { user -> user != null }
            // Convert the flow to a StateFlow<Boolean>
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                // If currentUser is not null, the initial value should be true (logged in)
                initialValue = (Firebase.auth.currentUser != null)
            )
    }

    override suspend fun transferDreamsFromAnonymousToPermanent(
        permanentUid: String,
        anonymousUid: String
    ) {
        @Serializable
        data class HandleAccountLinkingResponse(
            val success: Boolean = false,
            val message: String? = null
        )
        withContext(Dispatchers.IO) {
            try {
                val result = Firebase.functions
                    .httpsCallable("handleAccountLinking")
                    .invoke(
                        mapOf("permanentUid" to permanentUid, "anonymousUid" to anonymousUid)
                    )

                // Decode into a typed, serializable model to avoid Any serializer issues
                val data = result.data<HandleAccountLinkingResponse>()
                val success = data.success

                if (success) {
                    println("Dreams transferred successfully. Proceed to clean up anonymous account if needed.")
                } else {
                    println("Failed to transfer dreams: ${data.message}")
                }
            } catch (e: Exception) {
                when (e) {
                    is FirebaseFunctionsException -> {
                        println("Firebase Functions error: code=${e.code}, details=${e.details}")
                    }
                    else -> {
                        println("Error transferring dreams: $e")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun addDreamTokensFlowListener(): Flow<Resource<String>> {
        val user = Firebase.auth.currentUser
        if (user == null) {
            // Immediately return a flow that emits an error
            return flowOf(Resource.Error("User is not logged in"))
        }

        val docRef = Firebase.firestore.collection(USERS).document(user.uid)

        // Create one flow with `flow {}`, then collect snapshots inside it
        return flow {
            // 1) Emit a loading state up front
            emit(Resource.Loading())

            // 2) Collect snapshots in this same flow builder
            docRef.snapshots().collect { snapshot ->
                val dreamTokens = snapshot.getDreamTokenBalance()
                _dreamTokens.value = dreamTokens
                _dailyTokenStreak.value = snapshot.getOptionalInt("dailyDreamTokenStreak")
                    ?: snapshot.getOptionalLong("dailyDreamTokenStreak")?.toInt()
                    ?: 0
                _dailyTokenCompletedWeeks.value = maxOf(
                    snapshot.getDailyTokenCompletedWeeks(),
                    _dailyTokenStreak.value / DAILY_TOKEN_STREAK_BONUS_INTERVAL
                )
                val today = kotlin.time.Clock.System.todayIn(TimeZone.UTC).toString()
                val lastClaimDay = snapshot.getOptionalString("lastDailyDreamTokenClaimDay")
                    ?: snapshot.getOptionalString("lastDailyDreamTokenClaimDate")
                _lastDailyTokenClaimDay.value = lastClaimDay
                val claimedToday = if (lastClaimDay == today) {
                    snapshot.getOptionalInt("lastDailyDreamTokensAwarded")
                        ?: snapshot.getOptionalLong("lastDailyDreamTokensAwarded")?.toInt()
                        ?: 1
                } else {
                    0
                }
                _dailyTokensClaimedToday.value = claimedToday
                val dailyTokenAllowance = snapshot.getOptionalInt("lastDailyDreamTokenAllowance")
                    ?: snapshot.getOptionalLong("lastDailyDreamTokenAllowance")?.toInt()
                    ?: 1
                _hasClaimedDailyToken.value = claimedToday >= dailyTokenAllowance

                // Read hasGeneratedDreamWorld, default to false if not present
                val hasGenerated = try {
                    snapshot.get<Boolean>("hasGeneratedDreamWorld")
                } catch (e: Exception) {
                    // Field might not exist
                    false
                }
                _hasGeneratedDreamWorld.value = hasGenerated

                 // 3) Emit a success each time we get new data
                 emit(Resource.Success(dreamTokens.toString()))
             }
        }.catch { e ->
            // 4) Catch any exceptions thrown in the flow
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override suspend fun setHasGeneratedDreamWorld(hasGenerated: Boolean) {
        val user = Firebase.auth.currentUser ?: return
        val userDocRef = Firebase.firestore.collection(USERS).document(user.uid)
        try {
            userDocRef.update(mapOf("hasGeneratedDreamWorld" to hasGenerated))
        } catch (e: Exception) {
            Logger.e("AuthRepo") { "Failed to update hasGeneratedDreamWorld: $e" }
        }
    }
}

private const val DAILY_TOKEN_FREE_AWARD = 1
private const val DAILY_TOKEN_STREAK_BONUS_AWARD = 5
private const val DAILY_TOKEN_STREAK_BONUS_INTERVAL = 7

private fun DocumentSnapshot.getDreamTokenBalance(): Int {
    return getOptionalInt("dreamTokens")
        ?: getOptionalLong("dreamTokens")?.toInt()
        ?: getOptionalDouble("dreamTokens")?.toInt()
        ?: getOptionalString("dreamTokens")?.toIntOrNull()
        ?: 0
}

private fun DocumentSnapshot.hasDreamTokenField(): Boolean {
    return getOptionalInt("dreamTokens") != null ||
            getOptionalLong("dreamTokens") != null ||
            getOptionalDouble("dreamTokens") != null ||
            getOptionalString("dreamTokens") != null
}

private fun DocumentSnapshot.getDailyTokenCompletedWeeks(): Int {
    return getOptionalInt("dailyDreamTokenCompletedWeeks")
        ?: getOptionalLong("dailyDreamTokenCompletedWeeks")?.toInt()
        ?: getOptionalString("dailyDreamTokenCompletedWeeks")?.toIntOrNull()
        ?: 0
}

private fun DocumentSnapshot.getStringList(field: String): List<String> {
    return try {
        get<List<String>>(field).filter { it.isNotBlank() }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun DocumentSnapshot.getOptionalString(field: String): String? {
    return try {
        get<String>(field)
    } catch (_: Exception) {
        null
    }
}

private fun DocumentSnapshot.getOptionalInt(field: String): Int? {
    return try {
        get<Int>(field)
    } catch (_: Exception) {
        null
    }
}

private fun DocumentSnapshot.getOptionalLong(field: String): Long? {
    return try {
        get<Long>(field)
    } catch (_: Exception) {
        null
    }
}

private fun DocumentSnapshot.getOptionalDouble(field: String): Double? {
    return try {
        get<Double>(field)
    } catch (_: Exception) {
        null
    }
}

fun FirebaseUser.toUser(registrationTimestamp: Long) = mapOf(
     DISPLAY_NAME to displayName,
     EMAIL to email,
     CREATED_AT to serverTimestamp,
     "registrationTimestamp" to registrationTimestamp,
     "lastSignInTimestamp" to serverTimestamp,
    // Defaults for a brand new account (only applied on first doc creation)
    "dreamTokens" to 25L,
    "unlockedWords" to emptyList<String>(),
    "hasGeneratedDreamWorld" to false,
    "hasCompletedOnboarding" to false
 )
