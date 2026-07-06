package org.ballistic.dreamjournalai.shared.dream_premium.domain

import dreamjournalai.composeapp.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

enum class PremiumArtBundleKey(val metadataValue: String) {
    DreamMidnightV1("dream_midnight_v1");

    companion object {
        fun fromMetadata(paywallStyle: String?, heroAssetKey: String?): PremiumArtBundleKey {
            val style = paywallStyle.orEmpty()
            val hero = heroAssetKey.orEmpty()
            return entries.firstOrNull { bundle ->
                bundle.metadataValue.equals(style, ignoreCase = true) ||
                    bundle.metadataValue.equals(hero, ignoreCase = true)
            } ?: DreamMidnightV1
        }
    }
}

data class PremiumIconBundle(
    val premiumJournal: DrawableResource,
    val dreamToken: DrawableResource,
    val lessonBook: DrawableResource,
    val toolUnlock: DrawableResource,
    val audioWave: DrawableResource,
    val membershipStar: DrawableResource,
    val timelineNode: DrawableResource,
    val unlockIcon: DrawableResource,
    val dreamBell: DrawableResource,
    val annualBadge: DrawableResource,
)

data class PremiumArtBundle(
    val key: PremiumArtBundleKey,
    val promiseBenefitsHero: DrawableResource,
    val timelineHero: DrawableResource,
    val offerSupportArt: DrawableResource,
    val journalGiftBubble: DrawableResource,
    val icons: PremiumIconBundle,
)

fun resolvePremiumArtBundle(
    paywallStyle: String?,
    heroAssetKey: String?,
): PremiumArtBundle {
    return when (PremiumArtBundleKey.fromMetadata(paywallStyle, heroAssetKey)) {
        PremiumArtBundleKey.DreamMidnightV1 -> PremiumArtBundle(
            key = PremiumArtBundleKey.DreamMidnightV1,
            promiseBenefitsHero = Res.drawable.membership_promise_benefits_hero,
            timelineHero = Res.drawable.membership_tools_hero,
            offerSupportArt = Res.drawable.membership_final_payment_mascot_hero,
            journalGiftBubble = Res.drawable.journal_membership_gift_bubble,
            icons = PremiumIconBundle(
                premiumJournal = Res.drawable.membership_icon_premium_journal_glass,
                dreamToken = Res.drawable.membership_icon_dream_token_glass,
                lessonBook = Res.drawable.membership_icon_lesson_book_glass,
                toolUnlock = Res.drawable.membership_icon_tool_unlock_glass,
                audioWave = Res.drawable.membership_icon_audio_wave_glass,
                membershipStar = Res.drawable.membership_icon_membership_star_glass,
                timelineNode = Res.drawable.membership_icon_timeline_node_glass,
                unlockIcon = Res.drawable.membership_locked_tool_hero,
                dreamBell = Res.drawable.membership_icon_dream_bell_glass,
                annualBadge = Res.drawable.membership_icon_annual_badge_glass,
            ),
        )
    }
}
