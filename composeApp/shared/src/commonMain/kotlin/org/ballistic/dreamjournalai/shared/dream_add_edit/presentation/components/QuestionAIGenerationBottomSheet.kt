package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AIType
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AddEditDreamState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import org.ballistic.dreamjournalai.shared.core.components.DreamTokenLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionAIGenerationBottomSheet(
    addEditDreamState: AddEditDreamState,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit,
    onDreamTokenClick: (amount: Int) -> Unit,
    onAdClick: () -> Unit,
    onClickOutside: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("Standard AI", "Advanced AI")
    val amount = selectedIndex
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        modifier = modifier.fillMaxWidth(),
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        onDismissRequest = onClickOutside,
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
                        text = "Dream Question",
                        style = MaterialTheme.typography.headlineSmall,
                        color = White,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DreamTokenLayout(
                        totalDreamTokens = addEditDreamState.dreamTokens
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                OutlinedTextField(
                    value = addEditDreamState.aiStates[AIType.QUESTION_ANSWER]?.question ?: "",
                    onValueChange = {
                        onAddEditDreamEvent(AddEditDreamEvent.ChangeQuestionOfDream(it))
                    },
                    label = {
                        Text(
                            text = "Ask your Question",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp, 16.dp, 16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors()
                )
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
                            onClick = { selectedIndex = index },
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
                    isAdButtonVisible = amount == 1,
                    onAdClick = { onAdClick() },
                    onDreamTokenClick = { onDreamTokenClick(amount) },
                    amount = amount
                )
            }
        },
        containerColor = LightBlack
    )
}