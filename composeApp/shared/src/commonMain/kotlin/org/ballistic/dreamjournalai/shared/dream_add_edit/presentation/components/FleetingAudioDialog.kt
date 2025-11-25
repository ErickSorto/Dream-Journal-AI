package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.dream_token
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.RedOrange
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.SkyBlue
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun FleetingAudioDialog(
    audioTimestamp: Long,
    audioDurationSeconds: Long,
    onDismiss: () -> Unit,
    onKeepForever: (cost: Int) -> Unit,
) {
    val tokensCost = calculateAudioCost(audioDurationSeconds)
    var timeLeft by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Live timer update
    LaunchedEffect(audioTimestamp) {
        // If timestamp is 0 (unsaved new recording), use current time for preview
        val effectiveTimestamp = if (audioTimestamp == 0L) kotlin.time.Clock.System.now().toEpochMilliseconds() else audioTimestamp
        val expirationTime = Instant.fromEpochMilliseconds(effectiveTimestamp).plus(30.days)
        
        while (true) {
            val now = kotlin.time.Clock.System.now()
            val duration = expirationTime - now
            if (duration.isNegative()) {
                timeLeft = "Expired"
            } else {
                val days = duration.inWholeDays
                val hours = duration.inWholeHours % 24
                val minutes = duration.inWholeMinutes % 60
                val seconds = duration.inWholeSeconds % 60
                timeLeft = "${days}d ${hours}h ${minutes}m ${seconds}s"
            }
            delay(1000)
        }
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        onDismissRequest = { onDismiss() },
        containerColor = LightBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Fleeting Audio",
                    style = MaterialTheme.typography.headlineSmall,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Placeholder for balance if needed, or just spacer
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "This audio recording will disappear in:",
                style = MaterialTheme.typography.bodyMedium,
                color = White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = timeLeft,
                style = MaterialTheme.typography.displaySmall,
                color = RedOrange,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Keep this recording forever?",
                style = MaterialTheme.typography.titleMedium,
                color = White
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onKeepForever(tokensCost)
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.dream_token),
                        contentDescription = "Dream Token",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Use $tokensCost Dream Tokens",
                        style = MaterialTheme.typography.titleMedium,
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Maybe Later",
                    style = MaterialTheme.typography.titleMedium,
                    color = White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun calculateAudioCost(durationSeconds: Long): Int {
    val minutes = durationSeconds / 60.0
    return when {
        minutes < 5 -> 1
        minutes < 10 -> 2
        minutes < 15 -> 3
        else -> 4
    }
}
