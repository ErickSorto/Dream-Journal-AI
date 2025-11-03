package org.ballistic.dreamjournalai.shared.firebase

import co.touchlab.kermit.Logger

// On iOS we do not call dev.gitlive's Firebase.initialize from shared code because the
// native iOS app (AppDelegate / iosAppApp) is responsible for configuring Firebase once.
actual fun initFirebaseIfRequired(context: Any?) {
    Logger.d { "[DJAI/FirebaseInit] iOS platform - skipping shared Firebase.initialize to avoid double configuration" }
}

