package com.github.tvbox.osc.ui.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailPlaybackOrientationTest {

    private fun target(sizeReady: Boolean, portraitVideo: Boolean) =
        DetailPlaybackOrientation.target(sizeReady = sizeReady, portraitVideo = portraitVideo)

    private fun command(landscape: Boolean, portraitVideo: Boolean) =
        DetailPlaybackCommands.fullScreenState(
            requested = true,
            DetailPlaybackFacts(landscape = landscape, portraitVideo = portraitVideo),
        )

    @Test
    fun sizeNotReady_doesNotFlipToLandscape() {
        assertEquals(DetailPlaybackOrientation.Target.Portrait, target(sizeReady = false, portraitVideo = false))
        assertEquals(DetailPlaybackOrientation.Target.Portrait, target(sizeReady = false, portraitVideo = true))
    }

    @Test
    fun portraitVideo_staysPortraitFullScreen() {
        assertEquals(DetailPlaybackOrientation.Target.Portrait, target(sizeReady = true, portraitVideo = true))
        val (full, rotating) = command(landscape = false, portraitVideo = true)
        assertTrue(full)
        assertFalse(rotating)
    }

    @Test
    fun landscapeVideo_rotatesToLandscape() {
        assertEquals(DetailPlaybackOrientation.Target.Landscape, target(sizeReady = true, portraitVideo = false))
        val (full, rotating) = command(landscape = false, portraitVideo = false)
        assertTrue(full)
        assertTrue(rotating)
    }
}
