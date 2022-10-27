package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.pages

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPage(
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Column() {
                Text(
                    text = "Dream Background", modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp, 16.dp, 16.dp, 0.dp), //bold
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Dream.dreamBackgroundColors.forEach { image ->
                        Box(
                            modifier = Modifier
                                .size(if (viewModel.dreamBackgroundColor.value == image) 60.dp else 50.dp)
                                .shadow(15.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                                .border(
                                    if (viewModel.dreamBackgroundColor.value == image) 5.dp else 0.dp,
                                    Color.Black.copy(alpha = 0.3f),
                                    CircleShape
                                )
                                .clickable {
                                    viewModel.onEvent(AddEditDreamEvent.ChangeColorBackground(image))
                                }
                        ) {
                            Image(
                                painter = painterResource(id = image),
                                contentDescription = "Color",
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                }
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                , horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Text(
                        text = "Lucid Dream", modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 0.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        checked = viewModel.dreamIsLucid.value.value, onCheckedChange = {
                            viewModel.onEvent(AddEditDreamEvent.ChangeIsLucid(it))
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
                        text = "Nightmare", modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 0.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        checked = viewModel.dreamIsNightmare.value.value, onCheckedChange = {
                            viewModel.onEvent(AddEditDreamEvent.ChangeNightmare(it))
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
                        text = "Recurring Dream", modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 0.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        checked = viewModel.dreamIsRecurring.value.value, onCheckedChange = {
                            viewModel.onEvent(AddEditDreamEvent.ChangeRecurrence(it))
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
                        text = "False Awakening", modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(16.dp, 0.dp, 16.dp, 0.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        checked = viewModel.isFalseAwakening.value.value, onCheckedChange = {
                            viewModel.onEvent(AddEditDreamEvent.ChangeFalseAwakening(it))
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

                //slider for lucidity
                Text(
                    text = "Lucidity: " + viewModel.dreamLucidity.value.rating, modifier = Modifier
                        .fillMaxWidth()
                        .align(alignment = Alignment.CenterHorizontally)
                        .padding(16.dp, 16.dp, 16.dp, 0.dp)
                )

                Slider(
                    value = viewModel.dreamLucidity.value.rating.toFloat(),
                    onValueChange = {
                        viewModel.onEvent(AddEditDreamEvent.ChangeLucidity(it.toInt()))
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
                    text = "Vividness: " + viewModel.dreamVividness.value.rating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp, 16.dp, 16.dp, 0.dp)
                )

                Slider(
                    value = viewModel.dreamVividness.value.rating.toFloat(),
                    onValueChange = {
                        viewModel.onEvent(AddEditDreamEvent.ChangeVividness(it.toInt()))
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
                    text = "Mood: " + viewModel.dreamEmotion.value.rating, modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp, 16.dp, 16.dp, 0.dp)
                )

                Slider(
                    value = viewModel.dreamEmotion.value.rating.toFloat(),
                    onValueChange = {
                        viewModel.onEvent(AddEditDreamEvent.ChangeMood(it.toInt()))
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

            }
        }
    }
}

