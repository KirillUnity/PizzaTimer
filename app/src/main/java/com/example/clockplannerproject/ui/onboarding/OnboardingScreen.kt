package com.example.clockplannerproject.ui.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.SandRest
import com.example.clockplannerproject.ui.theme.TerracottaNow
import kotlinx.coroutines.launch

private data class IntroPage(
    @StringRes val kicker: Int,
    @StringRes val title: Int,
    @StringRes val body: Int,
)

private val IntroPages = listOf(
    IntroPage(R.string.onboarding_kicker_queue, R.string.onboarding_title_queue, R.string.onboarding_body_queue),
    IntroPage(R.string.onboarding_kicker_place, R.string.onboarding_title_place, R.string.onboarding_body_place),
    IntroPage(R.string.onboarding_kicker_step, R.string.onboarding_title_step, R.string.onboarding_body_step),
    IntroPage(R.string.onboarding_kicker_thread, R.string.onboarding_title_thread, R.string.onboarding_body_thread),
)

/**
 * Four-page thesis: queue, place, quiet step, next line.
 *
 * Skip and the last page both call [onFinished]. The caller persists "seen".
 * Controls sit inside the safe drawing area so the navigation bar cannot cover them.
 *
 * @param onFinished skip or the last page; persisted by the caller
 * @since 1.0.0
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { IntroPages.size })
    val scope = rememberCoroutineScope()
    val lastPage = pagerState.currentPage == IntroPages.lastIndex
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            IntroSlide(page = IntroPages[page], index = page)
        }
        PageMarks(current = pagerState.currentPage, count = IntroPages.size)
        IntroControls(
            lastPage = lastPage,
            onSkip = onFinished,
            onNext = {
                if (lastPage) {
                    onFinished()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
        )
    }
}

@Composable
private fun IntroSlide(
    page: IntroPage,
    index: Int,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(page.kicker),
            style = MaterialTheme.typography.labelLarge,
            color = TerracottaNow,
        )
        Text(
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            OnboardingFigure(
                page = index,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = stringResource(page.body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PageMarks(current: Int, count: Int) {
    val label = stringResource(R.string.onboarding_page, current + 1, count)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val selected = index == current
            Box(
                modifier = Modifier
                    .width(if (selected) 22.dp else 8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (selected) TerracottaNow else SandRest),
            )
        }
    }
}

@Composable
private fun IntroControls(
    lastPage: Boolean,
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val skipCd = stringResource(R.string.cd_onboarding_skip)
        TextButton(
            onClick = onSkip,
            modifier = Modifier.semantics { contentDescription = skipCd },
        ) {
            Text(
                text = stringResource(R.string.onboarding_skip),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val nextCd = stringResource(
            if (lastPage) R.string.cd_onboarding_done else R.string.cd_onboarding_next,
        )
        TextButton(
            onClick = onNext,
            modifier = Modifier.semantics { contentDescription = nextCd },
        ) {
            Text(
                text = stringResource(
                    if (lastPage) R.string.onboarding_done else R.string.onboarding_next,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingScreenPreview() {
    ClockPlannerProjectTheme {
        OnboardingScreen(onFinished = {})
    }
}
