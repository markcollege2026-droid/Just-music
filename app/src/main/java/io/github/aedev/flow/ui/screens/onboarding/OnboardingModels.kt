package io.github.aedev.flow.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import io.github.aedev.flow.R
import io.github.aedev.flow.data.backup.ImportSource
import io.github.aedev.flow.data.model.Channel
import io.github.aedev.flow.ui.isMusicEdition

internal const val MIN_TOPICS = 3
internal const val STAGGER_DELAY_MS = 50L

/** The setup steps in order. Welcome and Ready bookend the four steps the progress bar counts. */
enum class OnboardingStep(
    @StringRes val labelRes: Int?,
) {
    WELCOME(null),
    INTERESTS(R.string.onboarding_step_interests),
    CHANNELS(R.string.onboarding_step_channels),
    ARTISTS(R.string.onboarding_step_artists),
    ALERTS(R.string.onboarding_step_alerts),
    IMPORT(R.string.onboarding_step_import),
    READY(null),
    ;

    val index: Int get() = ordinal

    val showsProgress: Boolean get() = labelRes != null

    companion object {
        /**
         * The steps this edition walks, in order. The music edition has no video subscriptions, so
         * it skips the channel-picking step and goes from interests straight to artists.
         */
        val flow: List<OnboardingStep> = entries.filter { it != CHANNELS || !isMusicEdition }

        val progressSteps: List<OnboardingStep> = flow.filter { it.showsProgress }

        /** The step after/earlier than [step] in this edition, or null at either end. */
        fun stepAfter(step: OnboardingStep): OnboardingStep? {
            val index = flow.indexOf(step)
            return if (index < 0) flow.lastOrNull() else flow.getOrNull(index + 1)
        }

        fun stepBefore(step: OnboardingStep): OnboardingStep? {
            val index = flow.indexOf(step)
            return if (index < 0) null else flow.getOrNull(index - 1)
        }

        fun firstStep(): OnboardingStep = flow.first()
    }
}

@Immutable
data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.entries.first(),
    val topics: Set<String> = emptySet(),
    val query: String = "",
    val results: List<Channel> = emptyList(),
    val searching: Boolean = false,
    val subscribed: List<Channel> = emptyList(),
    val notifying: Set<String> = emptySet(),
    val importedSources: Set<ImportSource> = emptySet(),
    val completed: Boolean = false,
) {
    val topicsLeft: Int get() = (MIN_TOPICS - topics.size).coerceAtLeast(0)

    val canAdvance: Boolean get() = step != OnboardingStep.INTERESTS || topicsLeft == 0

    fun isSubscribed(channelId: String): Boolean = subscribed.any { it.id == channelId }
}
