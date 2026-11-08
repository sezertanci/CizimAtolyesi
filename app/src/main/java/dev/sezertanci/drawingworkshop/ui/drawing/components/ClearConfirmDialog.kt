package dev.sezertanci.drawingworkshop.ui.drawing.components

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
import dev.sezertanci.drawingworkshop.ui.components.EmojiIcon
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors

private val DialogShape = RoundedCornerShape(24.dp)
private val DialogPadding = 28.dp
private val DialogSpacing = 24.dp
private val AnswerButtonSize = 96.dp
private val TitleSize = 26.sp

/** Resmi temizlemeden önce sorulan soru. Kutunun dışına dokunmak "hayır" sayılır. */
@Composable
fun ClearConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DialogSpacing),
            modifier = Modifier
                .background(WorkshopColors.ItemBackground, DialogShape)
                .padding(DialogPadding),
        ) {
            EmojiIcon(CLEAR_EMOJI)
            Text(text = stringResource(R.string.clear_confirm_title), fontSize = TitleSize, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(DialogSpacing)) {
                PanelButton(
                    selected = false,
                    description = stringResource(R.string.clear_confirm_yes),
                    onClick = onConfirm,
                    size = AnswerButtonSize,
                ) { EmojiIcon(YES_EMOJI) }
                PanelButton(
                    selected = false,
                    description = stringResource(R.string.clear_confirm_no),
                    onClick = onDismiss,
                    size = AnswerButtonSize,
                ) { EmojiIcon(NO_EMOJI) }
            }
        }
    }
}

internal const val CLEAR_EMOJI = "🧹"
private const val YES_EMOJI = "✅"
private const val NO_EMOJI = "❌"
