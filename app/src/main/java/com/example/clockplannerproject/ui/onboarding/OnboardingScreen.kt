package com.example.clockplannerproject.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import kotlinx.coroutines.launch

/**
 * Four-page introduction: dial, leftover, center Start, empty-day task.
 *
 * Skip and the last page both call [onFinished]. The caller persists "seen".
 *
 * @param onFinished skip or the last page; persisted by the caller
 * @since 1.0.0
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = listOf(
        R.string.onboarding_title_dial to R.string.onboarding_body_dial,
        R.string.onboarding_title_leftover to R.string.onboarding_body_leftover,
        R.string.onboarding_title_start to R.string.onboarding_body_start,
        R.string.onboarding_title_empty to R.string.onboarding_body_empty,
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val lastPage = pagerState.currentPage == pages.lastIndex
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            val (titleRes, bodyRes) = pages[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(bodyRes),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val skipCd = stringResource(R.string.cd_onboarding_skip)
            TextButton(
                onClick = onFinished,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = skipCd },
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val nextCd = stringResource(
                if (lastPage) R.string.cd_onboarding_done else R.string.cd_onboarding_next,
            )
            TextButton(
                onClick = {
                    if (lastPage) {
                        onFinished()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = nextCd },
            ) {
                Text(
                    text = stringResource(
                        if (lastPage) R.string.onboarding_done else R.string.onboarding_next,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    ClockPlannerProjectTheme {
        OnboardingScreen(onFinished = {})
    }
}
