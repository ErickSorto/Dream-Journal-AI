package org.ballistic.dreamjournalai.shared.dream_maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.maintenance_message
import org.ballistic.dreamjournalai.shared.core.components.TypewriterText
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun MaintenanceScreen(
) {
    val animationDuration = 5000
    val showButton = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .background(
                    color = OriginalXmlColors.DarkBlue.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ),
        ) {
            TypewriterText(
                text = stringResource(Res.string.maintenance_message),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                animationDuration = animationDuration,
                onAnimationComplete = { showButton.value = true }
            )
        }
    }
}
