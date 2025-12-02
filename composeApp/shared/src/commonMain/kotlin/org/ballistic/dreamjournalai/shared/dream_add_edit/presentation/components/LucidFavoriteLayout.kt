package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.*
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AddEditDreamState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LucidFavoriteLayout(
    addEditDreamState: AddEditDreamState,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp, 0.dp, 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            LucidCustomButton(
                isLucid = addEditDreamState.dreamInfo.dreamIsLucid,
                onAddEditDreamEvent = onAddEditDreamEvent
            )
            RecurringCustomButton(
                isRecurring = addEditDreamState.dreamInfo.dreamIsRecurring,
                onAddEditDreamEvent = onAddEditDreamEvent
            )
            FalseAwakeningCustomButton(
                isFalseAwakening = addEditDreamState.dreamInfo.dreamIsFalseAwakening,
                onAddEditDreamEvent = onAddEditDreamEvent
            )
            NightmareCustomButton(
                isNightmare = addEditDreamState.dreamInfo.dreamIsNightmare,
                onAddEditDreamEvent = onAddEditDreamEvent
            )
            FavoriteCustomButton(
                isFavorite = addEditDreamState.dreamInfo.dreamIsFavorite,
                onAddEditDreamEvent = onAddEditDreamEvent
            )
        }
    }
}


@Composable
fun LucidCustomButton(
    isLucid: Boolean,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .size(68.dp)
            .clickable {
                onAddEditDreamEvent(AddEditDreamEvent.ChangeIsLucid(!isLucid))
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            painter = painterResource(Res.drawable.lighthouse_vector),
            contentDescription = stringResource(Res.string.lucid),
            modifier = Modifier.size(36.dp),
            tint = if (isLucid) OriginalXmlColors.SkyBlue else OriginalXmlColors.White.copy(
                alpha = 0.5f
            )
        )

        Text(
            text = stringResource(Res.string.lucid),
            color = OriginalXmlColors.White,
            style = typography.labelSmall
        )
    }
}

@Composable
fun FavoriteCustomButton(
    isFavorite: Boolean,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .size(68.dp)
            .clickable {
                onAddEditDreamEvent(AddEditDreamEvent.ChangeFavorite(!isFavorite))
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            painter = painterResource(Res.drawable.baseline_star_24),
            contentDescription = stringResource(Res.string.favorite),
            modifier = Modifier.size(36.dp),
            tint = if (isFavorite) OriginalXmlColors.Yellow else OriginalXmlColors.White.copy(
                alpha = 0.5f
            )
        )
        Text(
            text = stringResource(Res.string.favorite), color = OriginalXmlColors.White,
            style = typography.labelSmall
        )
    }
}

@Composable
fun NightmareCustomButton(
    isNightmare: Boolean,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .size(68.dp)
            .clickable {
                onAddEditDreamEvent(AddEditDreamEvent.ChangeNightmare(!isNightmare))
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            painter = painterResource(if (isNightmare) Res.drawable.nightmare_ghost_open else Res.drawable.nightmare_ghost_closed),
            contentDescription = if (isNightmare) stringResource(Res.string.nightmare_selected) else stringResource(
                Res.string.nightmare_unselected
            ),
            modifier = Modifier.size(36.dp),
            tint = if (isNightmare) OriginalXmlColors.RedOrange else OriginalXmlColors.White.copy(alpha = 0.5f)
        )
        Text(
            text = stringResource(Res.string.nightmare),
            color = OriginalXmlColors.White,
            style = typography.labelSmall
        )
    }
}

@Composable
fun FalseAwakeningCustomButton(
    isFalseAwakening: Boolean,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .size(68.dp)
            .clickable {
                onAddEditDreamEvent(AddEditDreamEvent.ChangeFalseAwakening(!isFalseAwakening))
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            painter = painterResource(Res.drawable.false_awakening_icon),
            contentDescription = stringResource(Res.string.false_awakening),
            modifier = Modifier.size(36.dp),
            tint = if (isFalseAwakening) OriginalXmlColors.Purple else OriginalXmlColors.White.copy(
                alpha = 0.5f
            )
        )
        Text(
            text = stringResource(Res.string.false_awakening),
            color = OriginalXmlColors.White,
            style = typography.labelSmall
        )
    }
}

@Composable
fun RecurringCustomButton(
    isRecurring: Boolean,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .size(68.dp)
            .clickable {
                onAddEditDreamEvent(AddEditDreamEvent.ChangeRecurrence(!isRecurring))
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            painter = painterResource(Res.drawable.baseline_cached_24),
            contentDescription = stringResource(Res.string.recurring),
            modifier = Modifier.size(36.dp),
            tint = if (isRecurring) OriginalXmlColors.Green else OriginalXmlColors.White.copy(
                alpha = 0.5f
            )
        )
        Text(
            text = stringResource(Res.string.recurring),
            color = OriginalXmlColors.White,
            style = typography.labelSmall
        )
    }
}