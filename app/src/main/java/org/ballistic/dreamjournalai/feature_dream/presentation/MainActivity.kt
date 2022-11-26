package org.ballistic.dreamjournalai.feature_dream.presentation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.MainScreenView
import org.ballistic.dreamjournalai.ui.theme.DreamCatcherAITheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DreamCatcherAITheme {
                // A surface container using the 'background' color from the theme
                ProvideWindowInsets(
                    windowInsetsAnimationsEnabled = true,
                    consumeWindowInsets = false,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreenView()
                    }
                }
            }
        }

    }
}

