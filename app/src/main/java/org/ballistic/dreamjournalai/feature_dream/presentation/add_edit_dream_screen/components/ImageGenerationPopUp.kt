package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.ballistic.dreamjournalai.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.viewmodel.AddEditDreamState
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.viewmodel.AddEditDreamViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ImageGenerationPopUp(
    addEditDreamState: AddEditDreamState,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit,
    onDreamTokenClick: () -> Unit,
    onAdClick: () -> Unit,
    onClickOutside: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = {
            onClickOutside()
        },
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    ,
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp, 8.dp, 16.dp, 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //Dream Painter
                    Text(
                        text = "Dream Painter",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colorResource(id = R.color.black),
                        modifier = Modifier.padding(8.dp)
                    )
                    OutlinedTextField(
                        value = addEditDreamState.dreamGeneratedDetails.response,
                        onValueChange = {
                            onAddEditDreamEvent(AddEditDreamEvent.ChangeDetailsOfDream(it))
                        },
                        label = {
                            Text(
                                text = "Explanation for Image",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(8.dp, 8.dp, 8.dp, 16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = false,
                        maxLines = 2,
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
                    AdTokenLayout(
                        onAdClick = onAdClick,
                        onDreamTokenClick = onDreamTokenClick,
                        amount = 2
                    )
                }
            }

        }
    )
}

