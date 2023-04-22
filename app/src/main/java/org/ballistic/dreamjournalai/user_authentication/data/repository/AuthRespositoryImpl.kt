package org.ballistic.dreamjournalai.user_authentication.data.repository


import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.*
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import org.ballistic.dreamjournalai.core.Constants.CREATED_AT
import org.ballistic.dreamjournalai.core.Constants.DISPLAY_NAME
import org.ballistic.dreamjournalai.core.Constants.EMAIL
import org.ballistic.dreamjournalai.core.Constants.SIGN_IN_REQUEST
import org.ballistic.dreamjournalai.core.Constants.SIGN_UP_REQUEST
import org.ballistic.dreamjournalai.core.Constants.USERS
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.user_authentication.domain.repository.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private var oneTapClient: SignInClient,
    @Named(SIGN_IN_REQUEST)
    private var signInRequest: BeginSignInRequest,
    @Named(SIGN_UP_REQUEST)
    private var signUpRequest: BeginSignInRequest,
    private var signInClient: GoogleSignInClient,
    private val db: FirebaseFirestore
) : AuthRepository {

    override val currentUser get() = auth.currentUser
    private val _isUserExist = MutableStateFlow(false)
    override val isUserExist: StateFlow<Boolean> = _isUserExist

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _emailVerified = MutableStateFlow(false)
    override val emailVerified: StateFlow<Boolean> = _emailVerified

    // Other properties and functions

    private val _dreamTokens = MutableStateFlow(0)
    override val dreamTokens: StateFlow<Int> = _dreamTokens


    init {
        setupUserDataListener()
    }

    init {
        validateUser()
    }

    override suspend fun consumeDreamTokens(tokensToConsume: Int): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            val user = currentUser
            if (user == null) {
                Resource.Error("User not found.")
            } else {
                val userDocRef = db.collection(USERS).document(user.uid)
                val snapshot = userDocRef.get().await()
                val currentDreamTokens = snapshot.getLong("dreamTokens")?.toInt() ?: 0

                if (currentDreamTokens >= tokensToConsume) {
                    userDocRef.update("dreamTokens", currentDreamTokens - tokensToConsume).await()
                    Resource.Success(true)
                } else {
                    Resource.Error("Not enough dream tokens available.")
                }
            }
        }
    }

    private fun validateUser() {
        val user = auth.currentUser
        _isUserExist.value = user != null
        _emailVerified.value = user?.isEmailVerified ?: false
        _isLoggedIn.value = user != null && user.isEmailVerified

        // Update the emailVerified field in Firestore
        if (user != null && user.isEmailVerified) {
            val userDocRef = db.collection(USERS).document(user.uid)
            userDocRef.update("emailVerified", true)
                .addOnSuccessListener {
                    Log.d("Firestore", "Email verification status updated successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error updating email verification status: ", exception)
                }
        }
    }

    private fun setupUserDataListener() {
        auth.currentUser?.let { user ->
            val userDocRef = db.collection(USERS).document(user.uid)

            userDocRef.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle the error case
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val dreamTokensValue = snapshot.getLong("dreamTokens")?.toInt() ?: 0
                    _dreamTokens.value = dreamTokensValue
                }
            }
        }
    }


    override suspend fun oneTapSignInWithGoogle(): OneTapSignInResponse {
        return try {
            val signInResult = oneTapClient.beginSignIn(signInRequest).await()
            Resource.Success(signInResult)
        } catch (e: Exception) {
            try {
                val signUpResult = oneTapClient.beginSignIn(signUpRequest).await()
                Resource.Success(signUpResult)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    override suspend fun firebaseSignInWithGoogle(
        googleCredential: AuthCredential
    ): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            emit(
                Resource.Success(
                    auth.signInWithCredential(googleCredential).await()
                )
            )
        }.catch { e ->
            emit(Resource.Error(e.toString()))
        }
    }

    private suspend fun addUserToFirestore(registrationTimestamp: Long) {
        auth.currentUser?.apply {
            val user = toUser(registrationTimestamp)
            db.collection(USERS).document(uid).set(user).await()
        }
    }

    override suspend fun firebaseSignUpWithEmailAndPassword(
        email: String, password: String
    ): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            Log.d("SignUp", "User created successfully: $authResult")
            // Add the user to Firestore with the additional fields
            addUserToFirestore(
                registrationTimestamp = System.currentTimeMillis()
            )
            emit(Resource.Success(authResult))
        }.catch { e ->
            Log.e("SignUp", "Error creating user: $e")
            emit(Resource.Error(e.toString()))
        }
    }

    override suspend fun sendEmailVerification(): Flow<Resource<Boolean>> {
        return flow {
            emit(Resource.Loading())
            val isEmailSent = auth.currentUser?.sendEmailVerification()?.await() != null
            Log.d("EmailVerification", "Email verification sent: $isEmailSent")
            emit(Resource.Success(isEmailSent))
        }.catch { e ->
            Log.e("EmailVerification", "Error sending email verification: $e")
            emit(Resource.Error(e.toString()))
        }
    }

    override suspend fun firebaseSignInWithEmailAndPassword(
        email: String,
        password: String,
    ): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            emit(
                Resource.Success(
                    auth.signInWithEmailAndPassword(email, password).await()
                )
            )
            validateUser()
        }.catch { e ->
            emit(Resource.Error(e.toString()))
        }
    }

    override suspend fun reloadFirebaseUser(): ReloadUserResponse {
        return try {
            auth.currentUser?.reload()?.await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): SendPasswordResetEmailResponse {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override fun signOut() = auth.signOut()

    override suspend fun revokeAccess(
        password: String?
    ): Flow<RevokeAccessResponse> {
        return flow {
            emit(Resource.Loading())
            auth.currentUser?.apply {
                if (password != null) {
                    val credential = EmailAuthProvider.getCredential(email!!, password)
                    reauthenticate(credential).await()
                }
                signInClient.revokeAccess().await()
                oneTapClient.signOut().await()
                delete().await()
            }

            emit(Resource.Success(true))
        }.catch { e ->
            emit(Resource.Error(e.toString()))
        }
    }

    override fun getAuthState(viewModelScope: CoroutineScope) = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser == null)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), auth.currentUser == null)
}

fun FirebaseUser.toUser(registrationTimestamp: Long) = mapOf(
    DISPLAY_NAME to displayName,
    EMAIL to email,
    CREATED_AT to serverTimestamp(),
    "registrationTimestamp" to registrationTimestamp,
    "dreamTokens" to 10,
)