package org.ballistic.dreamjournalai.shared.dream_store.domain

import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.dream_ad_free_description
import dreamjournalai.composeapp.shared.generated.resources.dream_ad_free_title
import dreamjournalai.composeapp.shared.generated.resources.dream_token_slide_title
import dreamjournalai.composeapp.shared.generated.resources.dream_benefit_interpretation
import dreamjournalai.composeapp.shared.generated.resources.dream_benefit_no_ads
import dreamjournalai.composeapp.shared.generated.resources.dream_benefit_painting
import dreamjournalai.composeapp.shared.generated.resources.dream_benefit_words
import dreamjournalai.composeapp.shared.generated.resources.dream_dictionary_description
import dreamjournalai.composeapp.shared.generated.resources.dream_dictionary_title
import dreamjournalai.composeapp.shared.generated.resources.dream_interpretation_description
import dreamjournalai.composeapp.shared.generated.resources.dream_interpretation_title
import dreamjournalai.composeapp.shared.generated.resources.dream_painting_description
import dreamjournalai.composeapp.shared.generated.resources.dream_painting_title
import dreamjournalai.composeapp.shared.generated.resources.dream_token_benefit
import dreamjournalai.composeapp.shared.generated.resources.dream_token_slide_benefit_1
import dreamjournalai.composeapp.shared.generated.resources.dream_token_slide_benefit_2
import dreamjournalai.composeapp.shared.generated.resources.dream_token_slide_benefit_3
import dreamjournalai.composeapp.shared.generated.resources.dream_token_slide_benefit_4
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class DreamTokenBenefit(
    val title: StringResource,
    val description: StringResource? = null,
    val benefit1: StringResource? = null,
    val benefit2: StringResource? = null,
    val benefit3: StringResource? = null,
    val benefit4: StringResource? = null,
    val image: DrawableResource
) {
    DreamTokenSlideBenefit(
        title = Res.string.dream_token_slide_title,
        benefit1 = Res.string.dream_token_slide_benefit_1,
        benefit2 = Res.string.dream_token_slide_benefit_2,
        benefit3 = Res.string.dream_token_slide_benefit_3,
        benefit4 = Res.string.dream_token_slide_benefit_4,
        image = Res.drawable.dream_token_benefit
    ),
    DreamPainting(
        title = Res.string.dream_painting_title,
        description = Res.string.dream_painting_description,
        image = Res.drawable.dream_benefit_painting
    ),
    DreamInterpretation(
        title = Res.string.dream_interpretation_title,
        description = Res.string.dream_interpretation_description,
        image = Res.drawable.dream_benefit_interpretation
    ),
    DreamDictionary(
        title = Res.string.dream_dictionary_title,
        description = Res.string.dream_dictionary_description,
        image = Res.drawable.dream_benefit_words
    ),
    DreamAdFree(
        title = Res.string.dream_ad_free_title,
        description = Res.string.dream_ad_free_description,
        image = Res.drawable.dream_benefit_no_ads
    ),
}
