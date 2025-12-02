package org.ballistic.dreamjournalai.shared.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.export_dreams_description
import dreamjournalai.composeapp.shared.generated.resources.export_dreams_title
import dreamjournalai.composeapp.shared.generated.resources.pdf_format
import dreamjournalai.composeapp.shared.generated.resources.txt_format
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDreamsBottomSheet(
    modifier: Modifier = Modifier,
    onPdfClick: () -> Unit,
    onTxtClick: () -> Unit,
    onClickOutside: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        sheetState = sheetState,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        onDismissRequest = onClickOutside,
        content = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp, 0.dp, 16.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.export_dreams_title),
                    style = Typography().titleLarge,
                    color = OriginalXmlColors.BrighterWhite
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(Res.string.export_dreams_description),
                    style = Typography().bodySmall,
                    color = OriginalXmlColors.BrighterWhite

                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = onPdfClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OriginalXmlColors.Purple
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.pdf_format),
                        style = Typography().headlineLarge,
                        color = OriginalXmlColors.White
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = onTxtClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OriginalXmlColors.SkyBlue
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.txt_format),
                        style = Typography().headlineLarge,
                        color = OriginalXmlColors.White
                    )
                }
            }
        },
        containerColor = OriginalXmlColors.LightBlack

    )
}
