package com.zakafir.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zakafir.domain.model.MosqueDetails

@Composable
fun DetailsScreen(
    mosque: MosqueDetails,
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AsyncImage(
                model = mosque.image,
                contentDescription = mosque.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        item {
            Text(
                mosque.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(mosque.associationName ?: "", style = MaterialTheme.typography.bodyMedium)
            Text(mosque.localisation ?: "localisation not available", style = MaterialTheme.typography.bodySmall)
        }

        item {
            Text("📞 ${mosque.phone}")
            Text("✉️ ${mosque.email}")
            mosque.site?.let { Text("🌐 $it") }
            mosque.paymentWebsite?.let { Text("💳 Donation: $it") }
        }

        item {
            Text(
                "Facilities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            val facilities = listOfNotNull(
                "Women's space".takeIf { mosque.womenSpace == true },
                "Janaza prayer".takeIf { mosque.janazaPrayer == true },
                "Aid prayer".takeIf { mosque.aidPrayer == true },
                "Children courses".takeIf { mosque.childrenCourses == true },
                "Adult courses".takeIf { mosque.adultCourses == true },
                "Ramadan meal".takeIf { mosque.ramadanMeal == true },
                "Handicap access".takeIf { mosque.handicapAccessibility == true },
                "Ablutions".takeIf { mosque.ablutions == true },
                "Parking".takeIf { mosque.parking == true }
            )
            facilities.forEach { Text("• $it") }
        }

        item {
            Text(
                "Prayer Times",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            mosque.times.forEachIndexed { index, time ->
                Text("• $time (Iqama: ${mosque.iqama.getOrNull(index) ?: "-"})")
            }
        }

        item {
            Text(
                "Jumu'a: ${mosque.jumua ?: "-"}",
                style = MaterialTheme.typography.titleMedium
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDetailsScreen() {
    DetailsScreen(
        MosqueDetails(
            uuid = "20c3c314-ab5f-4dff-b20f-ff7ffc736762",
            name = "Mosquée Ennour",
            type = "MOSQUE",
            slug = "ennour",
            latitude = 48.9196282,
            longitude = 2.3031679,
            associationName = "Association Ennour",
            phone = "0033147999285",
            paymentWebsite = "https://www.helloasso.com/associations/association-ennour/formulaires/1",
            email = "info@mosquee-gennevilliers.com",
            site = "http://www.mosquee-de-gennevilliers.com",
            closed = null,
            womenSpace = true,
            janazaPrayer = true,
            aidPrayer = true,
            childrenCourses = true,
            adultCourses = true,
            ramadanMeal = true,
            handicapAccessibility = true,
            ablutions = true,
            parking = true,
            times = listOf("05:18", "06:41", "14:00", "17:54", "21:13", "22:48"),
            iqama = listOf("+10", "+10", "+10", "+5", "+0"),
            jumua = "13:15",
            label = "Mosquée Ennour",
            localisation = "81 rue Paul Vaillant Couturier 92230 Gennevilliers France",
            image = "https://cdn.mawaqit.net/images/backend/mosque/20c3c314-ab5f-4dff-b20f-ff7ffc736762/mosque/1eea8d9c-5cc4-6e70-b448-066ed47a7584.jpg",
            jumua2 = null,
            jumua3 = null,
            jumuaAsDuhr = false,
            iqamaEnabled = true
        ),
        onCancel = {},
        onConfirm = {}
    )
}