package org.ballistic.dreamjournalai.shared.dream_add_edit.domain

enum class ImageStyle(
    val displayName: String,
    val promptAffix: String,
    val image: String
) {
    VIBRANT(
        "Vibrant",
        ", ultra-vibrant colors, dreamy atmosphere, painterly lighting, high detail, rich fantasy tones",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FVibrantDream.png?alt=media&token=8d16286e-833b-40e5-a219-d3f1e488c59e"
    ),

    REALISTIC(
        "Realistic",
        ", semi-photorealistic, cinematic lighting, ultra-detailed textures, 8k clarity, breathtaking natural realism",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FWormwholeWhale.png?alt=media&token=529c054c-c5b7-4b31-941c-b7bfcb8314ac"
    ),
    ANIME(
        "Anime",
        ", anime style, Studio Ghibli aesthetic, soft lighting, whimsical scenery, hand-painted vibe, vibrant yet gentle colors",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FGhibliDream.png?alt=media&token=ea5fb2f4-c046-4cfe-982a-297798f27551"
    ),
    FANTASY(
        "Fantasy",
        ", dreamy fantasy aesthetic, glowing atmosphere, enchanted landscapes, magical lighting, epic whimsical scenery, storybook wonder",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FFantasyDream.png?alt=media&token=5d3149ea-500d-4c72-a55c-485f05656158"
    ),
    NIGHTMARE(
        "Nightmare",
        ", dark horror aesthetic, eerie atmosphere, foggy shadows, unsettling tension, creepy lighting, haunting scenery, gothic mood",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FScaryDream.png?alt=media&token=4d49af92-8263-4fb3-86d4-aa541ec4eb90"
    ),
    SCI_FI(
        "Sci-Fi",
        ", futuristic sci-fi aesthetic, neon glow, sleek technology, cosmic atmosphere, advanced structures, cinematic sci-fi lighting, otherworldly landscapes",
        "https://firebasestorage.googleapis.com/v0/b/dream-journal-ai.appspot.com/o/DreamStyleImages%2FSciFiDream.png?alt=media&token=29bf94df-ae7d-4f86-9b69-1d47dafe2ebd"
    )
}