package org.ballistic.dreamjournalai.shared.dream_journal_list.data


import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.storage
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.ballistic.dreamjournalai.shared.core.Constants.USERS
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.domain.Flag
import org.ballistic.dreamjournalai.shared.core.util.decodeUrlPart
import org.ballistic.dreamjournalai.shared.core.util.downloadImageBytes
import org.ballistic.dreamjournalai.shared.core.util.readFileBytes
import org.ballistic.dreamjournalai.shared.core.util.toGitLiveData
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.repository.DreamRepository
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val logger = Logger.withTag("DreamRepositoryImpl")

class DreamRepositoryImpl(
    private val db: FirebaseFirestore
) :
    DreamRepository {

    private val dreamsCollection
        get() = getCollectionReferenceForDreams()

    private val flagsCollection: CollectionReference
        get() = db.collection("flags")

    private var currentDreamId: String = ""

    // Add this function to get the current user's UID
    private fun userID(): String? {
        return Firebase.auth.currentUser?.uid
    }

    override fun getDreams(): Flow<List<Dream>> {
        val collection = getCollectionReferenceForDreams()
            ?: return flowOf(emptyList())

        // Keep mapping only; drop per-emission logging to reduce noise
        return collection
            .snapshots()
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { doc ->
                    doc.toDream()
                }
            }
    }

    private fun DocumentSnapshot.toDream(): Dream? {
        val dream = data<Dream>()

        return dream.copy(
            id = this.id,
            uid = this.reference.parent.parent?.id ?: ""
        )
    }

    override suspend fun getDream(id: String): Resource<Dream> {
        return try {
            val docRef = getCollectionReferenceForDreams()?.document(id)
                ?: return Resource.Error("User not logged in or reference is null")

            val snapshot = docRef.get()

            if (snapshot.exists) {
                // Ensure id/uid are populated on single reads as well
                val dream = snapshot.getDream()
                Resource.Success(dream)
            } else {
                Resource.Error("Dream not found")
            }

        } catch (e: Exception) {
            Resource.Error("Error fetching dream: ${e.message}")
        }
    }

    private fun DocumentSnapshot.getDream(): Dream {
        val base = data<Dream>()
        return base.copy(
            id = this.id,
            uid = this.reference.parent.parent?.id ?: ""
        )
    }



    override suspend fun getCurrentDreamId(): Resource<String> {
        return Resource.Success(currentDreamId)
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun insertDream(dream: Dream): Resource<Unit> {
        logger.d { "insertDream: upsert id=${dream.id} httpImage=${dream.generatedImage.startsWith("http")}" }
        return try {
            // Ensure a stable ID for document and storage path
            val ensuredId = dream.id?.takeIf { it.isNotBlank() } ?: Uuid.random().toString()
            val ensuredUid = userID() ?: ""

            val existingDream = if (dream.id.isNullOrBlank()) null else getDream(dream.id)

            @OptIn(ExperimentalEncodingApi::class)
            suspend fun uploadImageIfNeeded(d: Dream): Dream {
                if (d.generatedImage.isBlank()) return d
                // If it's already a Firebase Storage URL, keep it
                val alreadyStorageUrl = d.generatedImage.startsWith("https://firebasestorage.googleapis.com/")
                if (alreadyStorageUrl) return d

                return withContext(Dispatchers.IO) {
                    // Overwrite in-place at a stable location derived from dream ID
                    val targetPath = "$ensuredUid/images/$ensuredId.jpg"
                    val storageRef = Firebase.storage.reference(location = targetPath)

                    val isDataUrl = d.generatedImage.startsWith("data:")
                    val imageBytes = if (isDataUrl) {
                        val commaIdx = d.generatedImage.indexOf(',')
                        val b64 = if (commaIdx >= 0) d.generatedImage.substring(commaIdx + 1) else ""
                        if (b64.isBlank()) ByteArray(0) else Base64.decode(b64)
                    } else {
                        downloadImageBytes(d.generatedImage)
                    }

                    val gitLiveData: Data = imageBytes.toGitLiveData()
                    storageRef.putData(gitLiveData)
                    val downloadUrl = storageRef.getDownloadUrl().toString()
                    val sep = if (downloadUrl.contains('?')) '&' else '?'
                    val version = kotlin.time.Clock.System.now().toEpochMilliseconds()
                    val finalUrl = "$downloadUrl${sep}v=$version"

                    // No need to delete the old image; overwrite happened in-place
                    d.copy(generatedImage = finalUrl)
                }
            }

            suspend fun uploadAudioIfNeeded(d: Dream): Dream {
                if (d.audioUrl.isBlank()) return d
                // If it's already a remote URL, assume it's uploaded
                if (d.audioUrl.startsWith("http")) return d

                return withContext(Dispatchers.IO) {
                    val targetPath = "$ensuredUid/dream_recordings/$ensuredId.m4a"
                    val storageRef = Firebase.storage.reference(location = targetPath)

                    // Read local file bytes
                    val audioBytes = readFileBytes(d.audioUrl)
                    if (audioBytes.isEmpty()) {
                        logger.w { "uploadAudioIfNeeded: Audio file empty or not found at ${d.audioUrl}" }
                        return@withContext d 
                    }

                    val gitLiveData: Data = audioBytes.toGitLiveData()
                    storageRef.putData(gitLiveData)
                    val downloadUrl = storageRef.getDownloadUrl().toString()
                    val sep = if (downloadUrl.contains('?')) '&' else '?'
                    val version = kotlin.time.Clock.System.now().toEpochMilliseconds()
                    val finalUrl = "$downloadUrl${sep}v=$version"

                    d.copy(audioUrl = finalUrl)
                }
            }

            if (existingDream is Resource.Success && existingDream.data != null) {
                val base = existingDream.data
                val updated = base.copy(
                    title = dream.title,
                    content = dream.content,
                    // Preserve original timestamp to keep list order stable
                    timestamp = base.timestamp,
                    date = dream.date,
                    sleepTime = dream.sleepTime,
                    wakeTime = dream.wakeTime,
                    AIResponse = dream.AIResponse,
                    isFavorite = dream.isFavorite,
                    isLucid = dream.isLucid,
                    isNightmare = dream.isNightmare,
                    isRecurring = dream.isRecurring,
                    falseAwakening = dream.falseAwakening,
                    lucidityRating = dream.lucidityRating,
                    moodRating = dream.moodRating,
                    vividnessRating = dream.vividnessRating,
                    timeOfDay = dream.timeOfDay,
                    backgroundImage = dream.backgroundImage,
                    generatedImage = dream.generatedImage,
                    generatedDetails = dream.generatedDetails,
                    dreamQuestion = dream.dreamQuestion,
                    dreamAIQuestionAnswer = dream.dreamAIQuestionAnswer,
                    dreamAIStory = dream.dreamAIStory,
                    dreamAIAdvice = dream.dreamAIAdvice,
                    dreamAIMood = dream.dreamAIMood,
                    audioUrl = dream.audioUrl,
                    audioTimestamp = dream.audioTimestamp,
                    audioDuration = dream.audioDuration,
                    isAudioPermanent = dream.isAudioPermanent,
                    audioTranscription = dream.audioTranscription,
                    id = ensuredId,
                    uid = ensuredUid
                )
                val updatedWithImage = uploadImageIfNeeded(updated)
                val updatedWithAudio = uploadAudioIfNeeded(updatedWithImage)
                getCollectionReferenceForDreams()?.document(ensuredId)?.set(updatedWithAudio)
            } else {
                val newDream = dream.copy(id = ensuredId, uid = ensuredUid)
                val newWithImage = uploadImageIfNeeded(newDream)
                val newWithAudio = uploadAudioIfNeeded(newWithImage)
                getCollectionReferenceForDreams()?.document(ensuredId)?.set(newWithAudio)
            }

            logger.d { "insertDream: success id=$ensuredId" }
            Resource.Success(Unit)
        } catch (e: Exception) {
            logger.e(e) { "insertDream: Error inserting dream" }
            Resource.Error("Error inserting dream: ${e.message}")
        }
    }

    fun parseFirebaseStoragePath(fullUrl: String): String {
        val regex = """https://firebasestorage.googleapis.com/v0/b/[^/]+/o/(.*)\?""".toRegex()
        val match = regex.find(fullUrl)
        return match?.groupValues?.get(1)?.replace("%2F", "/") // Decode URL-encoded path
            ?: throw IllegalArgumentException("Invalid Firebase Storage URL: $fullUrl")
    }


    override suspend fun deleteDream(id: String): Resource<Unit> {
        return try {
            // 1) Fetch the dream first (to remove image/audio if present)
            val dreamResult = getDream(id)
            if (dreamResult is Resource.Success && dreamResult.data != null) {
                val dreamData = dreamResult.data

                // 2) If dream has a generated image, remove it from Firebase Storage
                if (dreamData.generatedImage.isNotBlank()) {
                    try {
                        val imageUrl = dreamData.generatedImage
                        val ktorUrl = Url(imageUrl)
                        val pathPortion = ktorUrl.encodedPath.substringAfter("/o/").substringBefore("?")
                        val decodedPath = decodeUrlPart(pathPortion)
                        val imageRef = Firebase.storage.reference(decodedPath)
                        imageRef.delete()
                    } catch (e: Exception) {
                        logger.w(e) { "deleteDream: Failed to delete image for dream $id" }
                    }
                }
                
                // 3) Delete audio if exists
                if (dreamData.audioUrl.isNotBlank()) {
                    try {
                        val audioUrl = dreamData.audioUrl
                        val ktorUrl = Url(audioUrl)
                        val pathPortion = ktorUrl.encodedPath.substringAfter("/o/").substringBefore("?")
                        val decodedPath = decodeUrlPart(pathPortion)
                        val audioRef = Firebase.storage.reference(decodedPath)
                        audioRef.delete()
                    } catch (e: Exception) {
                         logger.w(e) { "deleteDream: Failed to delete audio for dream $id" }
                    }
                }
            }

            // 5) Remove the dream doc from Firestore
            dreamsCollection?.document(id)?.delete()
            logger.d { "deleteDream: success id=$id" }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Error deleting dream: ${e.message}")
        }
    }

    override suspend fun flagDream(
        dreamId: String?,
        imageAddress: String?
    ): Resource<Unit> {
        if (dreamId.isNullOrBlank() && imageAddress.isNullOrBlank()) {
            return Resource.Error("Either dreamId or imageAddress must be provided to flag content.")
        }

        return try {
            val flag = Flag(
                dreamId = dreamId?.takeIf { it.isNotBlank() } ?: "",
                imageAddress = imageAddress?.takeIf { it.isNotBlank() } ?: ""
            )

            flagsCollection.add(flag)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Error flagging content: ${e.message}")
        }
    }

    private fun getCollectionReferenceForDreams(): CollectionReference? {
        val user = Firebase.auth.currentUser

        return user?.uid?.let { uid ->
            db.collection(USERS) // "USERS" is your top-level collection
                .document(uid)   // user's document
                .collection("my_dreams")  // the "my_dreams" subcollection
        }
    }
}