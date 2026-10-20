package io.github.aedev.flow.ui.screens.library

import io.github.aedev.flow.ui.components.library.LibrarySection
import io.github.aedev.flow.ui.isMusicEdition

/**
 * The library shelves the Just Music edition lists: what was played, the viewer's music playlists,
 * liked songs and downloads. The video shelves — Watch later, Liked videos, video playlists, saved
 * Shorts and notes — have no counterpart in a music-only shell.
 */
private val MusicEditionSections =
    setOf(
        LibrarySection.HISTORY,
        LibrarySection.MUSIC_PLAYLISTS,
        LibrarySection.LIKED_MUSIC,
        LibrarySection.DOWNLOADS,
    )

/** Saved Shorts goes with the Shorts content switch as well as its own. */
internal fun visibleContentSections(
    hidden: Set<LibrarySection>,
    separatePlaylistKinds: Boolean,
    shortsEnabled: Boolean,
): List<LibrarySection> =
    if (isMusicEdition) {
        // Playlist kinds stay split so the single music shelf can be listed without a video twin.
        LibrarySection.content(separatePlaylistKinds = true).filter { it in MusicEditionSections && it !in hidden }
    } else {
        LibrarySection
            .content(separatePlaylistKinds)
            .filter { it !in hidden && (shortsEnabled || it != LibrarySection.SAVED_SHORTS) }
    }

/** Notes goes with the notes switch in Content settings as well as its own. */
internal fun visibleSettingsSections(
    hidden: Set<LibrarySection>,
    notesEnabled: Boolean,
): List<LibrarySection> =
    LibrarySection.settingsAndData.filter { section ->
        section !in hidden &&
            (notesEnabled || section != LibrarySection.NOTES) &&
            !(isMusicEdition && section == LibrarySection.NOTES)
    }
