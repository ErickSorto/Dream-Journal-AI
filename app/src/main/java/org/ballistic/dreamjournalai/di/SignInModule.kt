package org.ballistic.dreamjournalai.di

import android.app.Application
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import dagger.Provides
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.core.Constants.SIGN_IN_REQUEST
import org.ballistic.dreamjournalai.core.Constants.SIGN_UP_REQUEST
import javax.inject.Named

@Provides
@Named(SIGN_IN_REQUEST)
fun provideSignInRequest(
    app: Application
) = BeginSignInRequest.builder()
    .setGoogleIdTokenRequestOptions(
        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
            .setSupported(true)
            .setServerClientId(app.getString(R.string.web_client_id))
            .setFilterByAuthorizedAccounts(true)
            .build())
    .setAutoSelectEnabled(true)
    .build()

@Provides
@Named(SIGN_UP_REQUEST)
fun provideSignUpRequest(
    app: Application
) = BeginSignInRequest.builder()
    .setGoogleIdTokenRequestOptions(
        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
            .setSupported(true)
            .setServerClientId(app.getString(R.string.web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build())
    .build()