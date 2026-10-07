package com.example.clockplannerproject.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme

private data class CategoryPreview(
    val titleRes: Int,
    val color: Color,
)

/**
 * Stub list of pastel categories. No Room or CRUD yet.
 *
 * @since 0.2.0
 */
@Composable
fun CategoriesScreen(modifier: Modifier = Modifier) {
    val items = listOf(
        CategoryPreview(R.string.category_work, Color(0xFFA8C5A0)),
        CategoryPreview(R.string.category_rest, Color(0xFFD9CBB8)),
        CategoryPreview(R.string.category_sleep, Color(0xFFB8A9C9)),
        CategoryPreview(R.string.category_sport, Color(0xFFE8B4B8)),
        CategoryPreview(R.string.category_personal, Color(0xFFE8C4A8)),
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.categories_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.categories_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(item.color),
                )
                Text(
                    text = stringResource(item.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoriesScreenPreview() {
    ClockPlannerProjectTheme {
        CategoriesScreen()
    }
}
