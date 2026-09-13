package com.example.quicknote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quicknote.ui.theme.DarkSurfaceVariant
import com.example.quicknote.ui.theme.OnPrimaryCyan
import com.example.quicknote.ui.theme.PrimaryCyan
import com.example.quicknote.ui.theme.TextMuted
import com.example.quicknote.ui.theme.TextSecondary
import com.example.quicknote.ui.viewmodel.FilterCategory

@Composable
fun CategoryFilterChips(
    selectedCategory: FilterCategory,
    totalCount: Int,
    notesCount: Int,
    tasksCount: Int,
    onCategorySelected: (FilterCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryChip(
            label = "Todas",
            count = totalCount,
            isSelected = selectedCategory == FilterCategory.ALL,
            onClick = { onCategorySelected(FilterCategory.ALL) }
        )

        CategoryChip(
            label = "📄 Notas",
            count = notesCount,
            isSelected = selectedCategory == FilterCategory.NOTES,
            onClick = { onCategorySelected(FilterCategory.NOTES) }
        )

        CategoryChip(
            label = "📅 Pendientes",
            count = tasksCount,
            isSelected = selectedCategory == FilterCategory.TASKS,
            onClick = { onCategorySelected(FilterCategory.TASKS) }
        )
    }
}

@Composable
private fun CategoryChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PrimaryCyan else DarkSurfaceVariant
    val textColor = if (isSelected) OnPrimaryCyan else TextSecondary
    val badgeBgColor = if (isSelected) OnPrimaryCyan.copy(alpha = 0.2f) else Color(0xFF1E2227)
    val badgeTextColor = if (isSelected) OnPrimaryCyan else TextMuted

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(badgeBgColor)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = badgeTextColor
            )
        }
    }
}
