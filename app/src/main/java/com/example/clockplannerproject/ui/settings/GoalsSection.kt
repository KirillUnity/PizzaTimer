package com.example.clockplannerproject.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.GoalBook

/**
 * Goal list. Shown in Settings, not on the dial.
 *
 * @param book current main goal and intermediate goals
 * @param onMain replaces the only main goal
 * @param onAddIntermediate adds one intermediate title
 * @since 1.2.0
 */
@Composable
fun GoalsSection(
    book: GoalBook,
    onMain: (String) -> Unit,
    onAddIntermediate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = book.mainTitle,
            onValueChange = onMain,
            label = { Text(stringResource(R.string.goals_main)) },
            modifier = Modifier.fillMaxWidth(),
        )
        book.intermediate.forEach { goal ->
            Text(goal.title)
        }
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text(stringResource(R.string.goals_intermediate)) },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = {
            onAddIntermediate(draft)
            draft = ""
        }) {
            Text(stringResource(R.string.goals_add))
        }
    }
}
