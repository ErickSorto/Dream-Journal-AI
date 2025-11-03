package org.ballistic.dreamjournalai.shared.core.components
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration

@Composable
actual fun Modifier.dynamicBottomNavigationPadding(): Modifier {
    val orientation = LocalConfiguration.current.orientation
    return if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        this
    } else {
        this.navigationBarsPadding()
    }
}
