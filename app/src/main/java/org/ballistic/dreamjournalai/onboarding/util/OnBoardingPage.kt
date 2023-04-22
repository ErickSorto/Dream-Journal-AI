package org.ballistic.dreamjournalai.onboarding.util

import androidx.annotation.DrawableRes
import org.ballistic.dreamjournalai.R

sealed class OnBoardingPage(
    @DrawableRes
    val image: Int,
    val title: String,
    val description: String
) {
    object First : OnBoardingPage(
        image = R.drawable.blue_lighthouse,
        title = "Dream Journal AI",
        description = ""
    )

    object Second : OnBoardingPage(
        image = R.drawable.dark_blue_lighthouse_background,
        title = "Paint and Interpret",
        description = "Paint your dream and let the AI interpret it for you!"
    )

    object Third : OnBoardingPage(
        image = R.drawable.dark_blue_moon,
        title = "Save your dreams",
        description = "Save your dreams in the cloud and access them from anywhere."
    )

    //sign in
    object Fourth : OnBoardingPage(
        image = R.drawable.blue_lighthouse,
        title = "Sign In",
        description = ""
    )
}
