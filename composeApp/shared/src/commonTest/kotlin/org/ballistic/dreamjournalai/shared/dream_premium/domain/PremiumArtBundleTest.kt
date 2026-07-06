package org.ballistic.dreamjournalai.shared.dream_premium.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PremiumArtBundleTest {

    @Test
    fun `unknown art metadata falls back to dream midnight bundle`() {
        val artBundle = resolvePremiumArtBundle(
            paywallStyle = "unknown_style",
            heroAssetKey = "unknown_hero",
        )

        assertEquals(PremiumArtBundleKey.DreamMidnightV1, artBundle.key)
        assertNotNull(artBundle.promiseBenefitsHero)
        assertNotNull(artBundle.journalGiftBubble)
        assertNotNull(artBundle.icons.unlockIcon)
    }

    @Test
    fun `known paywall style resolves matching art bundle`() {
        val artBundle = resolvePremiumArtBundle(
            paywallStyle = "dream_midnight_v1",
            heroAssetKey = null,
        )

        assertEquals(PremiumArtBundleKey.DreamMidnightV1, artBundle.key)
        assertNotNull(artBundle.timelineHero)
        assertNotNull(artBundle.icons.annualBadge)
    }
}
