package io.github.aedev.flow.ui.screens.library

import com.google.common.truth.Truth.assertThat
import io.github.aedev.flow.ui.components.library.LibrarySection
import io.github.aedev.flow.ui.isMusicEdition
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Test

class LibrarySectionVisibilityTest {
    @Test
    fun `nothing hidden shows every content section in order`() {
        assumeFalse("the full Flow shell", isMusicEdition)
        assertThat(visibleContentSections(emptySet(), separatePlaylistKinds = false, shortsEnabled = true))
            .containsExactly(
                LibrarySection.HISTORY,
                LibrarySection.PLAYLISTS,
                LibrarySection.WATCH_LATER,
                LibrarySection.LIKED_VIDEOS,
                LibrarySection.LIKED_MUSIC,
                LibrarySection.DOWNLOADS,
                LibrarySection.SAVED_SHORTS,
            ).inOrder()
    }

    @Test
    fun `a hidden section is left out`() {
        assertThat(visibleContentSections(setOf(LibrarySection.LIKED_VIDEOS), separatePlaylistKinds = false, shortsEnabled = true))
            .doesNotContain(LibrarySection.LIKED_VIDEOS)
    }

    @Test
    fun `separate playlist kinds replace the combined section and hide independently`() {
        assumeFalse("the full Flow shell", isMusicEdition)
        val visible =
            visibleContentSections(setOf(LibrarySection.MUSIC_PLAYLISTS), separatePlaylistKinds = true, shortsEnabled = true)

        assertThat(visible).contains(LibrarySection.VIDEO_PLAYLISTS)
        assertThat(visible).containsNoneOf(LibrarySection.PLAYLISTS, LibrarySection.MUSIC_PLAYLISTS)
    }

    @Test
    fun `saved shorts follows the shorts content switch`() {
        assumeFalse("the full Flow shell", isMusicEdition)
        assertThat(visibleContentSections(emptySet(), separatePlaylistKinds = false, shortsEnabled = false))
            .doesNotContain(LibrarySection.SAVED_SHORTS)
    }

    @Test
    fun `notes follows the notes switch and every settings row can be hidden`() {
        assumeFalse("the full Flow shell", isMusicEdition)
        assertThat(visibleSettingsSections(emptySet(), notesEnabled = false))
            .containsExactly(LibrarySection.LOCAL_MEDIA, LibrarySection.SETTINGS)
            .inOrder()
        assertThat(visibleSettingsSections(LibrarySection.settingsAndData.toSet(), notesEnabled = true)).isEmpty()
    }

    @Test
    fun `the music edition lists music shelves only`() {
        assumeTrue(isMusicEdition)

        assertThat(visibleContentSections(emptySet(), separatePlaylistKinds = false, shortsEnabled = true))
            .containsExactly(
                LibrarySection.HISTORY,
                LibrarySection.MUSIC_PLAYLISTS,
                LibrarySection.LIKED_MUSIC,
                LibrarySection.DOWNLOADS,
            ).inOrder()
        assertThat(visibleSettingsSections(emptySet(), notesEnabled = true))
            .containsExactly(LibrarySection.LOCAL_MEDIA, LibrarySection.SETTINGS)
            .inOrder()
    }

    @Test
    fun `stored names that no longer exist are ignored`() {
        assertThat(LibrarySection.fromStored(setOf("LIKED_VIDEOS", "GONE"))).containsExactly(LibrarySection.LIKED_VIDEOS)
    }
}
