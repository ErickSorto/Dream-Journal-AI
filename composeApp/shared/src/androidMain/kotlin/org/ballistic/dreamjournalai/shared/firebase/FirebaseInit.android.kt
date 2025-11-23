package org.ballistic.dreamjournalai.shared.firebase

import android.content.Context
import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.initialize

actual fun initFirebaseIfRequired(context: Any?) {
    val ctx = context as? Context
    if (ctx == null) {
        Logger.w { "[DJAI/FirebaseInit] Android init called with null context - skipping" }
        return
    }

    try {
        //check if user exists\
        val user = Firebase.auth.currentUser
        if (user != null) {
            Logger.d { "[DJAI/FirebaseInit] User already exists - skipping" }
        } else {
            Logger.d { "[DJAI/FirebaseInit] User does not exist - initializing" }
        }

        Firebase.initialize(ctx)
        Logger.d { "[DJAI/FirebaseInit] Firebase.initialize(context) called on Android" }
    } catch (e: Exception) {
        Logger.e("DJAI/FirebaseInit") { "Firebase.initialize failed on Android: ${e.message}" }
    }
}
