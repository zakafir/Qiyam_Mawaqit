package com.zakafir.qiyam_mawaqit.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatChip(title: String, value: String, isHighlighted: Boolean = false) {
    Surface(
        tonalElevation = 2.dp, modifier = Modifier
            .clip(
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isHighlighted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}
