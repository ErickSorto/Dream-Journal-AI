package org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.components.ActionBottomSheet
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.viewmodel.InterpretDreamsScreenState
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.InterpretDreamsToolEvent
import org.jetbrains.compose.resources.stringResource
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.delete_this_interpretation_title
import dreamjournalai.composeapp.shared.generated.resources.delete_this_interpretation_message
import dreamjournalai.composeapp.shared.generated.resources.delete
import dreamjournalai.composeapp.shared.generated.resources.interpretation_deleted
import dreamjournalai.composeapp.shared.generated.resources.dismiss

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MassInterpretationHistoryPage(
    interpretDreamsScreenState: InterpretDreamsScreenState,
    pagerState: PagerState,
    scope: CoroutineScope,
    onEvent: (InterpretDreamsToolEvent) -> Unit
) {

    if (interpretDreamsScreenState.bottomDeleteCancelSheetState) {
        ActionBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.delete_this_interpretation_title),
            message = stringResource(Res.string.delete_this_interpretation_message),
            buttonText = stringResource(Res.string.delete),
            onClick = {
                onEvent(InterpretDreamsToolEvent.TriggerVibration)
                onEvent(InterpretDreamsToolEvent.ToggleBottomDeleteCancelSheetState(false))
                onEvent(InterpretDreamsToolEvent.DeleteMassInterpretation(interpretDreamsScreenState.chosenMassInterpretation))
                scope.launch {
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = StringValue.Resource(Res.string.interpretation_deleted),
                            action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {})
                        )
                    )
                }
            },
            onClickOutside = {
                onEvent(InterpretDreamsToolEvent.ToggleBottomDeleteCancelSheetState(false))
            }
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(interpretDreamsScreenState.massMassInterpretations) { interpretation ->
            MassInterpretationItem(
                interpretDreamsScreenState = interpretDreamsScreenState,
                pagerState = pagerState,
                massInterpretation = interpretation,
                onEvent = { onEvent(it) },
                scope = scope
            )
        }
    }
}
