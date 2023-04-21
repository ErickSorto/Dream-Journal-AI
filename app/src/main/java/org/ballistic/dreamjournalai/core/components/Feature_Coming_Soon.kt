package org.ballistic.dreamjournalai.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.TypewriterText

@Composable
fun FeatureComingSoonScreen(
    paddingValues: PaddingValues,
    onNavigateToAboutMeScreen: () -> Unit
) {
    val animationDuration = 5000
    val showButton = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            TypewriterText(
                text = "This feature is being actively worked on and will be available soon! Stay tuned!",
                modifier = Modifier,
                textAlign = TextAlign.Center,
                animationDuration = animationDuration,
                onAnimationComplete = { showButton.value = true }
            )
        }
        AnimatedVisibility(
            visible = showButton.value,
            modifier = Modifier
                .fillMaxWidth(.8f)
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(R.color.RedOrange) // Replace with your desired color
                ),
                onClick = { onNavigateToAboutMeScreen() }
            ) {
                Text(
                    text = "About Me",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}