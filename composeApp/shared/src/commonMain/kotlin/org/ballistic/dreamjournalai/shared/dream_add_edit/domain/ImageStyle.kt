package org.ballistic.dreamjournalai.shared.dream_add_edit.domain

enum class ImageStyle(
    val displayName: String,
    val promptAffix: String,
    val worldPromptAffix: String,
    val image: String,
    val worldPaintingImage: String
) {
    LET_AI_CHOOSE(
        "Let AI Choose",
        "",
        "",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FVibrantDream.png?alt=media&token=8d16286e-833b-40e5-a219-d3f1e488c59e",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FAIChooseDreamWorld.png?alt=media&token=efc1844f-620c-4e27-aae6-07fb357ec204"
    ),
    VIBRANT(
        "Vibrant",
        ", ultra-vibrant colors, dreamy atmosphere, painterly lighting, high detail, rich fantasy tones",
        ", unified vibrant fantasy dream landscape",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FVibrantDream.png?alt=media&token=8d16286e-833b-40e5-a219-d3f1e488c59e",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FVibrantDream.png?alt=media&token=8d16286e-833b-40e5-a219-d3f1e488c59e"
    ),

    REALISTIC(
        "Realistic",
        ", semi-photorealistic, cinematic lighting, ultra-detailed textures, 8k clarity, breathtaking natural realism",
        ", unified photorealistic cinematic nature landscape",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FWormwholeWhale.png?alt=media&token=529c054c-c5b7-4b31-941c-b7bfcb8314ac",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FRealisticDreamWorld.png?alt=media&token=87c89065-72f8-42b5-bf98-819dca311f3b"
    ),
    ANIME(
        "Anime",
        ", anime style, Studio Ghibli aesthetic, soft lighting, whimsical scenery, hand-painted vibe, vibrant yet gentle colors",
        ", unified anime studio ghibli landscape",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FGhibliDream.png?alt=media&token=ea5fb2f4-c046-4cfe-982a-297798f27551",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FAnimeDreamWorld.png?alt=media&token=984b30f2-a756-4994-93fe-82085c79305e"
    ),
    FANTASY(
        "Fantasy",
        ", dreamy fantasy aesthetic, glowing atmosphere, enchanted landscapes, magical lighting, epic whimsical scenery, storybook wonder",
        ", unified magical fantasy dream landscape",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FFantasyDream.png?alt=media&token=5d3149ea-500d-4c72-a55c-485f05656158",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FFantasyDreamWorld.png?alt=media&token=18ca103d-6f2e-4b6c-8b2e-baeb71b226c8"
    ),
    NIGHTMARE(
        "Nightmare",
        ", dark horror aesthetic, eerie atmosphere, foggy shadows, unsettling tension, creepy lighting, haunting scenery, gothic mood",
        ", unified dark horror gothic landscape",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FScaryDream.png?alt=media&token=4d49af92-8263-4fb3-86d4-aa541ec4eb90",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FNightmareDreamWorld.png?alt=media&token=8b57aec4-c1bc-48ab-8fac-8797e1f8ce67"
    ),
    SCI_FI(
        "Sci-Fi",
        ", futuristic sci-fi aesthetic, neon glow, sleek technology, cosmic atmosphere, advanced structures, cinematic sci-fi lighting, otherworldly landscapes",
        ", unified futuristic sci-fi neon landscape",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FSciFiDream.png?alt=media&token=29bf94df-ae7d-4f86-9b69-1d47dafe2ebd",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FSciFiDreamWorld.png?alt=media&token=680e8ff9-d2f5-4c6c-b099-de90ce961cd2"
    )
}
