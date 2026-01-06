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
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.advanced_ai
import dreamjournalai.composeapp.shared.generated.resources.standard_ai
import org.ballistic.dreamjournalai.shared.core.components.DreamTokenLayout
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamInterpretationPopUp(
    title: String,
    dreamTokens: Int,
    onAdClick: () -> Unit,
    onDreamTokenClick: (amount: Int) -> Unit,
    onClickOutside: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf(
        stringResource(Res.string.standard_ai),
        stringResource(Res.string.advanced_ai)
    )
    val amount = selectedIndex
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
                    .background(OriginalXmlColors.LightBlack)
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
                        color = OriginalXmlColors.White,
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
                            onClick = { selectedIndex = index },
                            selected = index == selectedIndex,
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = OriginalXmlColors.SkyBlue.copy(alpha = 0.8f),
                                activeContentColor = OriginalXmlColors.White,
                                inactiveContainerColor = Color.DarkGray.copy(alpha = 0.5f),
                                inactiveContentColor = OriginalXmlColors.White.copy(alpha = 0.7f),
                                activeBorderColor = OriginalXmlColors.SkyBlue
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                AdTokenLayout(
                    isAdButtonVisible = amount == 1,
                    onAdClick = {
                        onAdClick()
                    },
                    onDreamTokenClick = {
                        onDreamTokenClick(amount)
                    },
                    amount = amount
                )
            }
        },
        containerColor = OriginalXmlColors.LightBlack,
        scrimColor = Color.Transparent
    )
}