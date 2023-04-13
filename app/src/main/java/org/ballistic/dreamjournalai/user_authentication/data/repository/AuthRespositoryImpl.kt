package org.ballistic.dreamjournalai.user_authentication.data.repository


import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val db: FirebaseFirestore
) : AuthRepository {

    override val currentUser get() = auth.currentUser

    override fun isCurrentUserExist(): StateFlow<Boolean> {
        return MutableStateFlow(auth.currentUser != null)
    }
    override fun isEmailVerified(): StateFlow<Boolean> {
        val result = MutableStateFlow(auth.currentUser?.isEmailVerified ?: false)
        auth.currentUser?.let { user ->
            CoroutineScope(coroutineContext).launch {
                result.value = user.isEmailVerified
            }
        }
        return result
    }

    override suspend fun oneTapSignInWithGoogle(): OneTapSignInResponse {
        return try {
            val signInResult = oneTapClient.beginSignIn(signInRequest).await()
            Resource.Success(signInResult)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
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

    override suspend fun revokeAccess(): RevokeAccessResponse {
        return try {
            auth.currentUser?.delete()?.await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
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
    "registrationTimestamp" to registrationTimestamp
)