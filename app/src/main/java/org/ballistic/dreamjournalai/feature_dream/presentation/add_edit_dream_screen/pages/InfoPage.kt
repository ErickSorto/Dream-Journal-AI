package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockConfig
import com.maxkeppeler.sheets.clock.models.ClockSelection
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.viewmodel.AddEditDreamViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.DateAndTimeButtonsLayout
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.DreamImageSelectionRow
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.LucidFavoriteLayout
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.viewmodel.AddEditDreamState
import java.time.Clock
import java.time.LocalTime


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPage(
    dreamBackgroundImage: MutableState<Int>,
    addEditDreamState: AddEditDreamState,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit,
) {
    CalendarDialog(state = addEditDreamState.calendarState, config = CalendarConfig(
        monthSelection = true,
        yearSelection = true,
    ), selection = CalendarSelection.Date { date ->
        onAddEditDreamEvent(AddEditDreamEvent.ChangeDreamDate(date))
    })
    ClockDialog(state = addEditDreamState.sleepTimePickerState,
        config = ClockConfig(
            //default time 12:00am
            defaultTime = Clock.systemDefaultZone().instant().atZone(Clock.systemDefaultZone().zone)
                .toLocalTime(),
            is24HourFormat = false,
        ),
        selection = ClockSelection.HoursMinutes { hour, minute ->
            onAddEditDreamEvent(AddEditDreamEvent.ChangeDreamSleepTime(LocalTime.of(hour, minute)))
        }
    )

    ClockDialog(state = addEditDreamState.wakeTimePickerState,
        config = ClockConfig(
            //default time 12:00am
            defaultTime = Clock.systemDefaultZone().instant().atZone(Clock.systemDefaultZone().zone)
                .toLocalTime(),
            is24HourFormat = false,

            ),
        selection = ClockSelection.HoursMinutes { hour, minute ->
            onAddEditDreamEvent(AddEditDreamEvent.ChangeDreamWakeTime(LocalTime.of(hour, minute)))
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, start = 14.dp, end = 14.dp, top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Column {
                Text(
                    text = "Dream Background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp, 16.dp, 16.dp, 0.dp), //bold
                    style = typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                DreamImageSelectionRow(
                    dreamBackgroundImage = dreamBackgroundImage,
                    onAddEditDreamEvent = onAddEditDreamEvent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            //row for isLucid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LucidFavoriteLayout(
                    addEditDreamState = addEditDreamState,
                    onAddEditDreamEvent = onAddEditDreamEvent
                )
                DateAndTimeButtonsLayout(addEditDreamState = addEditDreamState)
                Row {
                    Text(
                        text = "Lucid Dream",
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 0.dp),
                        style = typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        checked = addEditDreamState.dreamInfo.dreamIsLucid,
                        onCheckedChange = {
                            onAddEditDreamEvent(AddEditDreamEvent.ChangeIsLucid(it))
                        },
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 0.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Red,
                            uncheckedThumbColor = Color.White,
                            checkedTrackColor = Color.Red.copy(alpha = 0.5f),
                            uncheckedTrackColor = Color.Black.copy(alpha = 0.3f),
                        )
                    )
                }

                //row for isNightmare
                Row {
                    Text(
                        text = "Nightmare",
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 0.dp),
                        style = typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        checked = addEditDreamState.dreamInfo.dreamIsNightmare,
                        onCheckedChange = {
                            onAddEditDreamEvent(AddEditDreamEvent.ChangeNightmare(it))
                        },
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 0.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Red,
                            uncheckedThumbColor = Color.White,
                            checkedTrackColor = Color.Red.copy(alpha = 0.5f),
                            uncheckedTrackColor = Color.Black.copy(alpha = 0.3f),
                        )
                    )
                }
                //isRecurring
                Row {
                    Text(
                        text = "Recurring Dream",
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 0.dp),
                        style = typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        checked = addEditDreamState.dreamInfo.dreamIsRecurring,
                        onCheckedChange = {
                            onAddEditDreamEvent(AddEditDreamEvent.ChangeRecurrence(it))
                        },
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 0.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Red,
                            uncheckedThumbColor = Color.White,
                            checkedTrackColor = Color.Red.copy(alpha = 0.5f),
                            uncheckedTrackColor = Color.Black.copy(alpha = 0.3f),
                        )
                    )
                }

                //row for false awakenings
                Row {
                    Text(
                        text = "False Awakening",
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 0.dp),
                        style = typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        checked = addEditDreamState.dreamInfo.dreamIsFalseAwakening,
                        onCheckedChange = {
                            onAddEditDreamEvent(AddEditDreamEvent.ChangeFalseAwakening(it))
                        },
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 16.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Red,
                            uncheckedThumbColor = Color.White,
                            checkedTrackColor = Color.Red.copy(alpha = 0.5f),
                            uncheckedTrackColor = Color.Black.copy(alpha = 0.3f),
                        )
                    )
                }

                //slider for lucidity
                Text(
                    text = "Lucidity: " + addEditDreamState.dreamInfo.dreamLucidity,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(alignment = Alignment.CenterHorizontally)
                        .padding(16.dp, 16.dp, 16.dp, 0.dp),
                    style = typography.bodyLarge
                )

                Slider(
                    value = addEditDreamState.dreamInfo.dreamLucidity.toFloat(),
                    onValueChange = {
                        onAddEditDreamEvent(AddEditDreamEvent.ChangeLucidity(it.toInt()))
                    },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp, 16.dp, 16.dp),
                    thumb = {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = Color.Red
                        )
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Black,
                        activeTrackColor = Color.Black,
                        inactiveTrackColor = Color.Black.copy(alpha = 0.3f)
                    )
                )

                //slider for vividness
                Text(
                    text = "Vividness: " + addEditDreamState.dreamInfo.dreamVividness,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp, 8.dp, 16.dp, 0.dp),
                    style = typography.bodyLarge
                )

                Slider(
                    value = addEditDreamState.dreamInfo.dreamVividness.toFloat(),
                    onValueChange = {
                        onAddEditDreamEvent(AddEditDreamEvent.ChangeVividness(it.toInt()))
                    },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp, 16.dp, 16.dp),
                    thumb = {
                        Icon(
                            imageVector = Icons.Filled.ShieldMoon,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = Color.Red
                        )
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Black,
                        activeTrackColor = Color.Black,
                        inactiveTrackColor = Color.Black.copy(alpha = 0.3f)
                    )
                )

                //slider for dreamMood
                Text(
                    text = "Mood: " + addEditDreamState.dreamInfo.dreamEmotion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp, 8.dp, 16.dp, 0.dp),
                    style = typography.bodyLarge
                )

                Slider(
                    value = addEditDreamState.dreamInfo.dreamEmotion.toFloat(),
                    onValueChange = {
                        onAddEditDreamEvent(AddEditDreamEvent.ChangeMood(it.toInt()))
                    },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp, 16.dp, 16.dp),
                    thumb = {
                        Icon(
                            imageVector = Icons.Filled.Mood,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = Color.Red
                        )
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Black,
                        activeTrackColor = Color.Black,
                        inactiveTrackColor = Color.Black.copy(alpha = 0.3f)
                    )
                )
                //AI details text field

                OutlinedTextField(
                    value = addEditDreamState.dreamGeneratedDetails.response,
                    onValueChange = {
                        onAddEditDreamEvent(AddEditDreamEvent.ChangeDetailsOfDream(it))
                    },
                    label = { Text(text = "Explanation for Image", style = typography.bodyLarge) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp, 8.dp, 16.dp),
                    textStyle = typography.bodyLarge,
                    singleLine = false,
                    maxLines = 5,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        disabledLabelColor = Color.Black,
                        disabledBorderColor = Color.Black,
                        textColor = Color.Black,
                        backgroundColor = Color.White.copy(alpha = 0.3f),
                        leadingIconColor = Color.Black,
                        trailingIconColor = Color.Black,
                        errorLabelColor = Color.Red,
                        errorBorderColor = Color.Red,
                        errorCursorColor = Color.Red
                    )
                )
            }
        }
    }
}