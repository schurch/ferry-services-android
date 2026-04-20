package com.stefanchurch.ferryservicesandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stefanchurch.ferryservicesandroid.data.model.ServiceStatus
import com.stefanchurch.ferryservicesandroid.ui.model.ServiceRowUiModel
import com.stefanchurch.ferryservicesandroid.ui.theme.FerryAmber
import com.stefanchurch.ferryservicesandroid.ui.theme.FerryGreen
import com.stefanchurch.ferryservicesandroid.ui.theme.FerryGrey
import com.stefanchurch.ferryservicesandroid.ui.theme.FerryRed

@Composable
fun ServiceStatusIndicator(status: ServiceStatus, modifier: Modifier = Modifier) {
    val color = statusColor(status)
    Box(modifier = modifier.size(24.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.25f)),
        )
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
        )
    }
}

@Composable
fun ServiceRowCard(
    row: ServiceRowUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ServiceStatusIndicator(row.status)
            Column(modifier = Modifier.weight(1f)) {
                Text(row.area, fontWeight = FontWeight.SemiBold)
                Text(
                    row.route,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    row.disruptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor(row.status),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.75f),
            )
        }
    }
}

@Composable
fun SectionHeading(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

@Composable
fun MetadataLine(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.size(12.dp))
        Text(value)
    }
}

fun statusColor(status: ServiceStatus): Color = when (status) {
    ServiceStatus.NORMAL -> FerryGreen
    ServiceStatus.DISRUPTED -> FerryAmber
    ServiceStatus.CANCELLED -> FerryRed
    ServiceStatus.UNKNOWN -> FerryGrey
}
