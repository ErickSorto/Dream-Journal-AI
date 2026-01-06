package org.ballistic.dreamjournalai.shared.dream_add_edit.data


import androidx.compose.ui.text.intl.Locale
import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.functions.functions
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.ballistic.dreamjournalai.shared.core.util.OpenAIApiKeyUtil
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.repository.DreamRepository
import kotlin.uuid.ExperimentalUuidApi

/**
 * High-level AI operations for dream processing (text + image).
 * Centralizes OpenAI usage away from the ViewModel to reduce verbosity and enable reuse.
 */
interface DreamAIService {
    suspend fun generateText(type: AITextType, dreamContent: String, cost: Int, extra: String? = null): AIResult<String>
    suspend fun generateDetails(dreamContent: String, cost: Int): AIResult<String>
    suspend fun generateImageFromDetails(details: String, cost: Int, style: String, model: String? = null): AIResult<String>
    suspend fun transcribeAudio(storagePath: String): AIResult<String>
}

sealed class AIResult<out T> {
    data class Success<T>(val data: T): AIResult<T>()
    data class Error(val message: String): AIResult<Nothing>()
}

enum class AITextType { TITLE, INTERPRETATION, ADVICE, MOOD, STORY, QUESTION_ANSWER, DREAM_WORLD_SUMMARY, MASS_INTERPRETATION }

@Serializable
data class TranscriptionResponse(val text: String)

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

    private suspend fun chat(
        prompt: String, 
        maxTokens: Int = 1500, 
        reasoningEffort: String = "low",
        verbosity: String = "low",
        model: String = "gpt-5.1"
    ): AIResult<String> {
        try {
            logger.d { "Sending chat request to $model. MaxTokens: $maxTokens, Reasoning: $reasoningEffort, Verbosity: $verbosity" }
            logger.v { "Prompt: $prompt" }

            val apiKey = OpenAIApiKeyUtil.getOpenAISecretKey()
            
            val bodyJson = buildJsonObject {
                put("model", model)
                put("messages", JsonArray(listOf(buildJsonObject {
                    put("role", "user")
                    put("content", prompt)
                })))
                put("max_completion_tokens", maxTokens)
                
                // Use top-level reasoning_effort for newer models instead of the reasoning object
                // The API spec can vary between reasoning_effort (top-level) and reasoning: { effort: ... }
                // Based on recent API updates for reasoning models (like o1), reasoning_effort is often standard.
                // However, if the error persists, it means the model does not support reasoning at all or the field name is incorrect.
                // Given the error "Unknown parameter: 'reasoning'", it likely expects `reasoning_effort` or nothing if not supported.
                if (model == "gpt-5.1") {
                   put("reasoning_effort", reasoningEffort)
                }
            }.toString()

            val response = httpClient.post("https://api.openai.com/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                headers { append("Authorization", "Bearer $apiKey") }
                setBody(bodyJson)
                timeout { requestTimeoutMillis = 70_000; connectTimeoutMillis = 30_000; socketTimeoutMillis = 70_000 }
            }.bodyAsText()

            val root = Json.parseToJsonElement(response).jsonObject
            val choices = root["choices"] as? JsonArray
            val content = choices?.firstOrNull()?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.content.orEmpty()

            if (content.isBlank()) {
                 val error = root["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content 
                 if (error != null) {
                     logger.e { "Chat API Error: $error" }
                     return AIResult.Error(error)
                 }
            }

            logger.d { "Chat completion received. Length: ${content.length}" }
            logger.v { "Content: $content" }
            return AIResult.Success(content)
        } catch (e: Exception) {
            logger.e { "chat error: ${e.message}" }
            return AIResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun generateText(type: AITextType, dreamContent: String, cost: Int, extra: String?): AIResult<String> {
        logger.d { "generateText called. Type: $type, Cost: $cost" }
        val locale = Locale.current
        
        // Default settings
        var reasoningEffort = if (cost <= 0) "low" else "high"
        var verbosity = if (cost <= 0) "low" else "medium"
        var model = "gpt-5.1"
        var maxTokens = 1500 

        if (type == AITextType.TITLE) {
            model = "gpt-5-nano"
            // reasoningEffort and verbosity are ignored for nano in chat()
        } else if (type == AITextType.DREAM_WORLD_SUMMARY) {
            reasoningEffort = "high"
            verbosity = "medium"
            maxTokens = 5000
        } else if (type == AITextType.MASS_INTERPRETATION) {
            // For mass interpretation, we likely want more tokens to handle multiple dreams
            maxTokens = 3000
            // reasoningEffort/verbosity already set by cost above
        }

        val prompt = when (type) {
            AITextType.TITLE -> "Please generate a very short title (max 5 words) for this dream. Optimize for concise length. No quotes. Do not include the word 'dream'. Dream content: $dreamContent"
            AITextType.INTERPRETATION -> {
                """Please provide a strict, concise, and objective interpretation of the following dream.
- Do NOT include conversational filler, greetings, or closing questions.
- Do NOT ask the user for follow-up or feedback.
- Focus strictly on the psychological and symbolic meaning.

$dreamContent

Markdown is supported. Use the following style guidelines:
- Begin with a brief introductory paragraph providing context.
- Format key interpretation points using this structure:
#### [A concise title of 1-5 words for the theme]

- The explanation text as a bullet point.
- Ensure there is a blank line between the heading and the bullet point.
- Use short paragraphs to maintain readability on mobile devices.
- Avoid excessive formatting or long paragraphs.
- The final paragraph should be a standalone summary and NOT a bullet point.
Respond in language: $locale""".trimIndent()
            }
            AITextType.ADVICE -> "Please give advice that can be obtained for this dream: $dreamContent. Respond in language: $locale"
            AITextType.MOOD -> "Please describe the mood of this dream: $dreamContent. Respond in language: $locale"
            AITextType.STORY -> "Please generate a very short story (concise) based on this dream: $dreamContent. Respond in language: $locale"
            AITextType.QUESTION_ANSWER -> {
                val q = extra ?: ""
                "Please answer the following question: $q as it relates to this dream: $dreamContent. Respond in language: $locale"
            }
            AITextType.DREAM_WORLD_SUMMARY -> {
                """Analyze the recurring themes and objects in the following dream entries. Your goal is to create a single, surreal, yet beautiful and coherent image prompt.

1.  **Identify 4-5 key visual symbols** from the dreams.
2.  **Choose ONE or TWO dominant, unifying scenes** from the dreams (e.g., a serene lake, a dense forest, a grand room).
3.  **Compose one vivid sentence** that describes a blended scene. Place the key symbols logically within this combined environment. For example, if blending a 'forest' and a 'library', the result could be 'towering bookshelves grow like ancient trees in a sun-dappled forest clearing'. The goal is a creative and beautiful fusion, not a simple list.

**Crucially, ensure the final sentence is safe for image generation.** Avoid sensitive terms (use 'figures' instead of specific ages).

Dream Entries:
$dreamContent

Respond only with the single, safe, and coherent sentence in the language '$locale'."""
            }
            AITextType.MASS_INTERPRETATION -> {
                """Analyze the following dreams to find common themes, symbols, and deeper meanings. Provide a strict, concise, and objective analysis.
- Do NOT include conversational filler, greetings, or closing questions.
- Do NOT ask the user for follow-up or feedback.
- Focus strictly on the psychological, symbolic, and recurring patterns across these dreams.

Dreams:
$dreamContent

Markdown is supported. Use the following style guidelines:
- Begin with a brief introductory paragraph providing context on the collection of dreams.
- Format key insights and recurring themes using this structure:
#### [A concise, 1-5 word title for the common theme]

- The explanation text as a bullet point.
- Ensure there is a blank line between the heading and the bullet point.
- Use short paragraphs to maintain readability on mobile devices.
- Avoid excessive formatting or long paragraphs.
- The final paragraph should be a standalone summary of the collective meaning and NOT a bullet point.
Respond in language: $locale""".trimIndent()
            }
        }
        return chat(prompt, maxTokens = maxTokens, reasoningEffort = reasoningEffort, verbosity = verbosity, model = model)
    }


    override suspend fun generateDetails(dreamContent: String, cost: Int): AIResult<String> {
        val reasoningEffort = if (cost <= 1) "low" else "high"
        val verbosity = if (cost <= 1) "low" else "medium"
        
        val basePrompt = if (cost <= 1) {
            """In one concise, third-person sentence (12-24 words), describe the dream's setting, mood, and any standout objects or figures. Focus on creating a clear, neutral visual foundation.

$dreamContent""".trimIndent()
        } else {
            """Analyze the dream content. Your goal is to create a single, surreal, yet beautiful and coherent image prompt.

1.  **Identify key visual symbols** from the dream.
2.  **Choose a dominant, unifying scene** (e.g., a serene lake, a dense forest).
3.  **Compose one vivid sentence (12-24 words)** that describes a blended scene. Place key symbols logically within this environment. The goal is a creative and beautiful fusion.

**Crucially, ensure the final sentence is safe for image generation.** Avoid sensitive terms.

Dream Content:
$dreamContent""".trimIndent()
        }
        return chat(basePrompt, maxTokens = 2000, reasoningEffort = reasoningEffort, verbosity = verbosity)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun generateImageFromDetails(details: String, cost: Int, style: String, model: String?): AIResult<String> {
        logger.d { "generateImageFromDetails called. Details length: ${details.length}, Cost: $cost, Style: $style, Model: $model" }
        val apiKey = OpenAIApiKeyUtil.getOpenAISecretKey()
        val primaryModel = model ?: if (cost <= 1) "mini" else "gpt-image-1"
        val fallbackModel = if (cost <= 1) "mini" else "gpt-image-1" 

        val gptSizeString = "1024x1024"
        val fallbackSizeString = "512x512"

        suspend fun generateWithKtor(model: String, prompt: String, size: String): AIResult<String> {
            logger.d { "Generating image with Ktor. Model: $model, Size: $size" }
            return try {
                val bodyJson = buildJsonObject {
                    put("model", model)
                    put("prompt", prompt)
                    put("size", size)
                    put("n", 1)
                }.toString()
                val response = httpClient.post("https://api.openai.com/v1/images/generations") {
                    contentType(ContentType.Application.Json)
                    headers { append("Authorization", "Bearer $apiKey") }
                    setBody(bodyJson)
                    timeout { requestTimeoutMillis = 70_000; connectTimeoutMillis = 30_000; socketTimeoutMillis = 70_000 }
                }.bodyAsText()
                logger.i { "Image generation response: $response" }
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

        val prompt = details.ifBlank { "A beautiful, peaceful dream scene" }
        val finalPrompt = "$prompt, $style"

        suspend fun generate(model: String): AIResult<String> {
            val normalizedModel = model.lowercase()
            // Determine size based on model
            val size = if (normalizedModel.contains("mini") || cost <= 1) fallbackSizeString else gptSizeString
            
            return generateWithKtor(normalizedModel, finalPrompt, size)
        }

        return when (val primaryResult = generate(primaryModel)) {
            is AIResult.Success -> primaryResult
            is AIResult.Error -> {
                logger.w { "Primary model ($primaryModel) failed with error: ${primaryResult.message}. Trying fallback ($fallbackModel)." }
                generate(fallbackModel)
            }
        }
    }

    override suspend fun transcribeAudio(storagePath: String): AIResult<String> {
        return try {
            val functions = Firebase.functions
            val result = functions
                .httpsCallable("transcribeAudio")
                .invoke(mapOf("storagePath" to storagePath))
            
            val response = result.data<TranscriptionResponse>()
            val text = response.text
            
            if (text.isNotBlank()) {
                AIResult.Success(text)
            } else {
                AIResult.Error("Empty response from Gemini")
            }
        } catch (e: Exception) {
            logger.e { "transcription error: ${e.message}" }
            AIResult.Error(e.message ?: "Unknown transcription error")
        }
    }
}
