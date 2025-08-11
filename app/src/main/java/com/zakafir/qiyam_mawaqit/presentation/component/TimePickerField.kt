package com.zakafir.qiyam_mawaqit.presentation.component

import androidx.compose.foundation.clickable
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun TimePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val parts = value.split(":")
    val initH = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 0
    val initM = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$label:")
        Text(
            value,
            modifier = Modifier.clickable {
                TimePickerDialog(
                    context,
                    { _, h, m ->
                        onValueChange(String.format(Locale.US, "%02d:%02d", h, m))
                    },
                    initH,
                    initM,
                    true
                ).show()
            }.background(
                color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            ).clip(
                androidx.compose.material3.MaterialTheme.shapes.small
            ).padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
        )
    }
}