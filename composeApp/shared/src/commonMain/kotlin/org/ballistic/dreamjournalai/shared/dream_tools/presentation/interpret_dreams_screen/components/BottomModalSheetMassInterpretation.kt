package org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.ballistic.dreamjournalai.shared.core.components.DreamTokenLayout
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.AdTokenLayout
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.InterpretDreamsToolEvent
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.viewmodel.InterpretDreamsScreenState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import org.jetbrains.compose.resources.stringResource
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.standard_ai
import dreamjournalai.composeapp.shared.generated.resources.advanced_ai

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomModalSheetMassInterpretation(
    title: String,
    dreamTokens: Int,
    interpretDreamsScreenState: InterpretDreamsScreenState,
    onEvent: (InterpretDreamsToolEvent) -> Unit,
    onAdClick: () -> Unit,
    onDreamTokenClick: (amount: Int) -> Unit,
    onClickOutside: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var amount by remember { mutableIntStateOf(interpretDreamsScreenState.chosenDreams.size) }
    
    val options = listOf(stringResource(Res.string.standard_ai), stringResource(Res.string.advanced_ai))
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        modifier = modifier.fillMaxWidth(),
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        onDismissRequest = {
            onClickOutside()
        },
        content = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(LightBlack)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = White,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DreamTokenLayout(
                        totalDreamTokens = dreamTokens
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = { 
                                selectedIndex = index
                                if (index == 0) {
                                    // Standard AI
                                    onEvent(InterpretDreamsToolEvent.UpdateModel("gpt-4o-mini"))
                                    amount = interpretDreamsScreenState.chosenDreams.size
                                } else {
                                    // Advanced AI
                                    onEvent(InterpretDreamsToolEvent.UpdateModel("gpt-4o"))
                                    amount = interpretDreamsScreenState.chosenDreams.size * 2
                                }
                            },
                            selected = index == selectedIndex,
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = OriginalXmlColors.SkyBlue.copy(alpha = 0.8f),
                                activeContentColor = White,
                                inactiveContainerColor = Color.DarkGray.copy(alpha = 0.5f),
                                inactiveContentColor = White.copy(alpha = 0.7f),
                                activeBorderColor = OriginalXmlColors.SkyBlue
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                AdTokenLayout(
                    isAdButtonVisible = amount <= 3,
                    onAdClick = {
                        onAdClick()
                    },
                    onDreamTokenClick = { amount ->
                        onDreamTokenClick(amount)
                    },
                    amount = amount
                )
            }
        },
        containerColor = LightBlack,
        scrimColor = Color.Transparent
    )
}