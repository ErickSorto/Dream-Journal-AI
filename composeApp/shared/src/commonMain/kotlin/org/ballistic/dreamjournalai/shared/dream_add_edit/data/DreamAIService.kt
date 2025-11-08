package org.ballistic.dreamjournalai.shared.dream_add_edit.data

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.PlatformLocale
import co.touchlab.kermit.Logger
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.util.OpenAIApiKeyUtil
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.repository.DreamRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * High-level AI operations for dream processing (text + image).
 * Centralizes OpenAI usage away from the ViewModel to reduce verbosity and enable reuse.
 */
interface DreamAIService {
    suspend fun generateText(type: AITextType, dreamContent: String, cost: Int, extra: String? = null): AIResult<String>
    suspend fun generateDetails(dreamContent: String, cost: Int): AIResult<String>
    suspend fun generateImageFromDetails(details: String, cost: Int): AIResult<String>
}

sealed class AIResult<out T> {
    data class Success<T>(val data: T): AIResult<T>()
    data class Error(val message: String): AIResult<Nothing>()
}

enum class AITextType { TITLE, INTERPRETATION, ADVICE, MOOD, STORY, QUESTION_ANSWER }

class DefaultDreamAIService(
    private val dreamRepository: DreamRepository
) : DreamAIService {

    private val logger = Logger.withTag("DreamAIService")

    private val httpClient by lazy {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 70_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 70_000
            }
            install(HttpRequestRetry) {
                maxRetries = 1
                retryIf { _, response -> response.status.value in 500..599 }
                retryOnExceptionIf { _, cause -> cause is HttpRequestTimeoutException }
                exponentialDelay()
            }
        }
    }

    private suspend fun chat(model: String, prompt: String, maxTokens: Int = 750, temperature: Double? = null): AIResult<String> = try {
        val apiKey = OpenAIApiKeyUtil.getOpenAISecretKey()
        val openAI = OpenAI(apiKey)
        val request = ChatCompletionRequest(
            model = ModelId(model),
            messages = listOf(ChatMessage(role = ChatRole.User, content = prompt)),
            maxTokens = maxTokens,
            temperature = temperature
        )
        val completion = openAI.chatCompletion(request)
        AIResult.Success(completion.choices.firstOrNull()?.message?.content.orEmpty())
    } catch (e: Exception) {
        logger.e { "chat error: ${e.message}" }
        AIResult.Error(e.message ?: "Unknown error")
    }

    override suspend fun generateText(type: AITextType, dreamContent: String, cost: Int, extra: String?): AIResult<String> {
        val locale = Locale.current.platformLocale
        val model = if (cost <= 0) "gpt-4.1-mini" else "gpt-4.1"
        val prompt = when (type) {
            AITextType.TITLE -> "Please generate a title for this dream with only 1 to 4 words, no quotes, and don't include the word dream: $dreamContent"
            AITextType.INTERPRETATION -> """Please interpret the following dream:

$dreamContent

Markdown is supported. Use the following style guidelines:
- Begin with an introductory paragraph to provide context.
- Use bullet points (`-`) for key elements.
- Start each bullet point with a **bolded title** (e.g., `**Title**:`).
- The title and body text should touch (no blank line between them).
- Ensure the body text is concise and directly follows the title.
- Titles should use the same size as the body text but be bold for emphasis.
- Use short paragraphs to maintain readability on mobile devices.
- Avoid excessive formatting or long paragraphs for better readability.
Respond in language: $locale""".trimIndent()
            AITextType.ADVICE -> "Please give advice that can be obtained for this dream: $dreamContent. Respond in language: $locale"
            AITextType.MOOD -> "Please describe the mood of this dream: $dreamContent. Respond in language: $locale"
            AITextType.STORY -> "Please generate a very short story (concise) based on this dream: $dreamContent. Respond in language: $locale"
            AITextType.QUESTION_ANSWER -> {
                val q = extra ?: ""
                "Please answer the following question: $q as it relates to this dream: $dreamContent. Respond in language: $locale"
            }
        }
        return chat(model, prompt, maxTokens = 750, temperature = if (type == AITextType.STORY) 1.0 else null)
    }

    override suspend fun generateDetails(dreamContent: String, cost: Int): AIResult<String> {
        val model = if (cost <= 1) "gpt-4.1-mini" else "gpt-4.1"
        val creativity = if (cost <= 1) .4 else 1.1
        val basePrompt = if (cost <= 1) {
            """You are a Dream Environment Artist. In one short, third-person sentence (8–20 words), describe the heart of the dream below — the setting, mood, and any objects or figures that stand out.

$dreamContent

Keep it gentle and visually poetic. Use soft colors, glowing light, and a calm sense of wonder. Focus on what can be seen or felt in a peaceful, beautiful way.""".trimIndent()
        } else {
            """You are a Dream Environment Artist. In one vivid, third-person sentence (8–20 words), portray the dream below as a scene full of atmosphere, hidden meaning, and emotional symbolism.

$dreamContent

Focus on recurring dream symbols — light, water, doors, skies, reflections, mirrors, or shifting landscapes — weaving them naturally into the visual. The mood should feel intentional and emotionally charged.""".trimIndent()
        }
        val styleSuffix = if (cost <= 1) ", kept simple and peaceful with a soft, serene atmosphere." else ", vibrant and layered with symbolic meaning, where beauty meets mystery and emotion. 4k photo hyper realistic scene"
        return when (val result = chat(model, basePrompt, maxTokens = 175, temperature = creativity)) {
            is AIResult.Success -> AIResult.Success(result.data + " " + styleSuffix)
            is AIResult.Error -> result
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun generateImageFromDetails(details: String, cost: Int): AIResult<String> {
        val apiKey = OpenAIApiKeyUtil.getOpenAISecretKey()
        val primaryModel = if (cost <= 1) "gpt-image-1-mini" else "dall-e-3"
        val fallbackModel = if (cost <= 1) "dall-e-2" else "gpt-image-1" // DALL-E 3 can fall back to gpt-image-1

        val gptSizeString = "1024x1024"
        val sdkSize = if (cost <= 1 || primaryModel == "dall-e-2" || fallbackModel == "dall-e-2") ImageSize.is512x512 else ImageSize.is1024x1024

        suspend fun generateWithKtor(model: String, prompt: String): AIResult<String> {
            return try {
                // Safely build JSON object and serialize to string
                val bodyJson = buildJsonObject {
                    put("model", model)
                    put("prompt", prompt)
                    put("size", gptSizeString)
                    put("n", 1)
                }.toString()
                val response = httpClient.post("https://api.openai.com/v1/images/generations") {
                    contentType(ContentType.Application.Json)
                    headers { append("Authorization", "Bearer $apiKey") }
                    setBody(bodyJson)
                    timeout { requestTimeoutMillis = 70_000; connectTimeoutMillis = 30_000; socketTimeoutMillis = 70_000 }
                }.bodyAsText()
                val root = Json.parseToJsonElement(response).jsonObject
                val dataNode = root["data"] ?: return AIResult.Error(root["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content ?: "No data in response")
                val data = dataNode as? JsonArray ?: return AIResult.Error("Malformed response: data not array")
                val first = data.firstOrNull()?.jsonObject ?: return AIResult.Error("Empty data array")
                val url = first["url"]?.jsonPrimitive?.content
                if (!url.isNullOrBlank()) {
                    return AIResult.Success(url)
                }
                val b64 = first["b64_json"]?.jsonPrimitive?.content
                if (!b64.isNullOrBlank()) {
                    return AIResult.Success("data:image/png;base64,$b64")
                }
                AIResult.Error("No url or b64_json in image response")
            } catch (e: Exception) {
                logger.e { "image ktor error: ${e.message}" }
                AIResult.Error(e.message ?: "Unknown image error")
            }
        }

        suspend fun generateWithSdk(model: String, prompt: String, size: ImageSize): AIResult<String> = try {
            val openAI = OpenAI(apiKey)
            val creation = ImageCreation(prompt = prompt, model = ModelId(model), n = 1, size = size, user = "url")
            val images = openAI.imageURL(creation)
            val url = images.firstOrNull()?.url.orEmpty()
            if (url.isBlank()) AIResult.Error("Empty image URL") else AIResult.Success(url)
        } catch (e: Exception) {
            logger.e { "image sdk error: ${e.message}" }
            AIResult.Error(e.message ?: "Unknown SDK error")
        }

        val prompt = details.ifBlank { "A beautiful, peaceful dream scene" }

        suspend fun generate(model: String): AIResult<String> {
            val normalizedModel = model.lowercase()
            return if (normalizedModel.startsWith("gpt-image-1")) {
                generateWithKtor(normalizedModel, prompt)
            } else {
                generateWithSdk(normalizedModel, prompt, sdkSize)
            }
        }

        val primaryResult = generate(primaryModel)

        return when (primaryResult) {
            is AIResult.Success -> {
                if (primaryResult.data.startsWith("data:image")) {
                    val dream = Dream(id = Uuid.random().toString(), generatedImage = primaryResult.data, title = "", content = "", timestamp = 0L)
                    dreamRepository.insertDream(dream)
                    val uploadedDream = dreamRepository.getDream(dream.id!!)
                    if (uploadedDream is Resource.Success) {
                        AIResult.Success(uploadedDream.data!!.generatedImage)
                    } else {
                        AIResult.Error("Failed to upload image")
                    }
                } else {
                    primaryResult
                }
            }
            is AIResult.Error -> {
                logger.w { "Primary model ($primaryModel) failed with error: ${primaryResult.message}. Trying fallback ($fallbackModel)." }
                generate(fallbackModel)
            }
        }
    }
}
