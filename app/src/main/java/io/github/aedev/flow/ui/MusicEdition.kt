package io.github.aedev.flow.ui

import io.github.aedev.flow.BuildConfig

/**
 * True in the Just Music edition (`BuildConfig.MUSIC_ONLY`).
 *
 * The music surface keeps its own components untouched; this flag only decides what the shell
 * offers around it: which root tabs exist, which library shelves are listed, and which onboarding
 * steps are walked. Screens that belong to the video side stay compiled and reachable from shared
 * code paths, so no call site has to care whether the edition shows them.
 */
internal val isMusicEdition: Boolean = BuildConfig.MUSIC_ONLY
