package dev.sezertanci.drawingworkshop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors

private val DialogShape = RoundedCornerShape(24.dp)
private val DialogPadding = 28.dp
private val DialogSpacing = 24.dp
private val AnswerButtonSize = 96.dp
private val TitleSize = 26.sp

/** Geri alınamayan bir işten önce sorulan evet / hayır sorusu. Kutunun dışına dokunmak "hayır" sayılır. */
@Composable
fun ConfirmDialog(emoji: String, title: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DialogSpacing),
            modifier = Modifier
                .background(WorkshopColors.ItemBackground, DialogShape)
                .padding(DialogPadding),
        ) {
            EmojiIcon(emoji)
            Text(text = title, fontSize = TitleSize, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(DialogSpacing)) {
                AnswerButton(YES_EMOJI, stringResource(R.string.confirm_yes), onConfirm)
                AnswerButton(NO_EMOJI, stringResource(R.string.confirm_no), onDismiss)
            }
        }
    }
}

@Composable
private fun AnswerButton(emoji: String, description: String, onClick: () -> Unit) {
    PanelButton(selected = false, description = description, onClick = onClick, size = AnswerButtonSize) {
        EmojiIcon(emoji)
    }
}

private const val YES_EMOJI = "✅"
private const val NO_EMOJI = "❌"
