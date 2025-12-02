package org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.dismiss
import dreamjournalai.composeapp.shared.generated.resources.only_15_dreams_can_be_selected
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.core.util.formatCustomDate
import org.ballistic.dreamjournalai.shared.core.util.parseCustomDate
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.components.DateHeader
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.components.DreamItem
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.InterpretDreamsToolEvent
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.viewmodel.InterpretDreamsScreenState


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectDreamsPage(
    interpretDreamsScreenState: InterpretDreamsScreenState,
    scope: CoroutineScope,
    chosenDreams: List<Dream>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 16.dp),
    onEvent: (InterpretDreamsToolEvent) -> Unit
) {

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        // Step 1: Parse and Sort Dreams
        val sortedGroupedDreams = interpretDreamsScreenState.dreams
            .mapNotNull { dream ->
                try {
                    val parsedDate = parseCustomDate(dream.date)
                    Pair(parsedDate, dream)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            .sortedWith(
                compareByDescending<Pair<LocalDate, Dream>> { it.first }
                    .thenByDescending { it.second.timestamp }
            )
            .groupBy { it.first }

        // Step 2: Iterate Through Groups
        sortedGroupedDreams.forEach { (date, dreams) ->

            // Sticky Header for the Date
            stickyHeader {
                DateHeader(dateString = formatCustomDate(date), paddingStart = 20)
            }


            items(dreams) { (_, dream) ->
                val isDreamChosen = chosenDreams.contains(dream)
                DreamItem(
                    dream = dream,
                    hasBorder = isDreamChosen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .padding(horizontal = 20.dp),
                    onClick = {
                        onEvent(InterpretDreamsToolEvent.TriggerVibration)
                        val chosenDreamSizeLimit = 15
                        if (chosenDreams.size >= chosenDreamSizeLimit && !chosenDreams.contains(
                                dream
                            )
                        ) {
                            scope.launch {
                                SnackbarController.sendEvent(
                                    SnackbarEvent(
                                        message = StringValue.Resource(Res.string.only_15_dreams_can_be_selected),
                                        action = SnackbarAction(
                                            name = StringValue.Resource(Res.string.dismiss),
                                            action = {}
                                        )
                                    )
                                )
                            }
                        } else {
                            onEvent(
                                InterpretDreamsToolEvent.ToggleDreamToInterpretationList(
                                    dream
                                )
                            )
                        }
                    },
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
